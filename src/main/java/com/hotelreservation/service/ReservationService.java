package com.hotelreservation.service;

import com.hotelreservation.config.KafkaTopicsConfig;
import com.hotelreservation.domain.Payment;
import com.hotelreservation.domain.Reservation;
import com.hotelreservation.domain.Room;
import com.hotelreservation.domain.RoomInventory;
import com.hotelreservation.domain.enums.PaymentStatus;
import com.hotelreservation.domain.enums.ReservationStatus;
import com.hotelreservation.dto.reservation.ReservationRequest;
import com.hotelreservation.dto.reservation.ReservationResponse;
import com.hotelreservation.event.EventPublisher;
import com.hotelreservation.event.InventoryEvent;
import com.hotelreservation.event.ReservationEvent;
import com.hotelreservation.exception.ResourceNotFoundException;
import com.hotelreservation.exception.RoomUnavailableException;
import com.hotelreservation.lock.DistributedLockService;
import com.hotelreservation.mapper.ReservationMapper;
import com.hotelreservation.repository.ReservationRepository;
import com.hotelreservation.repository.RoomInventoryRepository;
import com.hotelreservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * ==========================================================================
 *  HIGH-CONCURRENCY RESERVATION ENGINE
 * ==========================================================================
 * Defense-in-depth against double booking, using FOUR layered strategies. Each
 * layer catches races the previous one can miss:
 *
 *  1) REDIS DISTRIBUTED LOCK (cluster-wide, coarse):
 *     Serializes booking attempts for a given room ACROSS all app instances,
 *     before we touch the DB. Sheds contention early and keeps DB txns short.
 *
 *  2) PESSIMISTIC DB LOCK (SELECT ... FOR UPDATE, per-date inventory rows):
 *     Even with the Redis lock, a lock could expire mid-flight (TTL) or Redis
 *     could blip. Row locks give hard serialization at the source of truth for
 *     the exact (room, date) rows this booking needs.
 *
 *  3) OPTIMISTIC LOCK (@Version on inventory + reservation) + RETRY:
 *     For lower-contention updates we avoid holding row locks and instead let
 *     Hibernate detect a version clash, then retry a bounded number of times.
 *
 *  4) UNIQUE CONSTRAINT (room_id, stay_date) at the DB:
 *     The final backstop. If everything else somehow interleaves, the DB
 *     refuses the second insert/booked-flip. Correctness never depends on
 *     application timing alone.
 *
 *  Transaction runs at READ_COMMITTED (Postgres default). We deliberately do
 *  NOT rely on SERIALIZABLE for the whole flow: it would throttle throughput.
 *  Instead we scope strong locking to just the contended rows.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final RoomInventoryRepository inventoryRepository;
    private final PaymentService paymentService;
    private final DistributedLockService lockService;
    private final EventPublisher eventPublisher;
    private final ReservationMapper reservationMapper;

    @Value("${app.reservation.lock-ttl-seconds}")
    private long lockTtlSeconds;
    @Value("${app.reservation.payment-window-minutes}")
    private long paymentWindowMinutes;
    @Value("${app.reservation.max-retry}")
    private int maxRetry;

    /**
     * Full booking flow: PENDING -> LOCK_ROOM -> PAYMENT_PENDING -> CONFIRMED.
     * Layer 1 (Redis) wraps the transactional core; layers 2-4 live inside it.
     */
    public ReservationResponse book(ReservationRequest req, Long customerId) {
        validateDates(req.checkIn(), req.checkOut());
        String lockKey = DistributedLockService.roomLockKey(req.roomId());
        String token = lockService.tryLock(lockKey, Duration.ofSeconds(lockTtlSeconds));
        if (token == null) {
            throw new RoomUnavailableException("Room is being booked by another request, please retry");
        }
        try {
            Reservation reservation = executeBookingWithRetry(req, customerId);
            // Payment happens after inventory is safely held (PAYMENT_PENDING).
            return settlePayment(reservation, req);
        } finally {
            lockService.unlock(lockKey, token);
        }
    }

    /** Optimistic-retry wrapper around the pessimistic-locked transactional core. */
    private Reservation executeBookingWithRetry(ReservationRequest req, Long customerId) {
        int attempt = 0;
        while (true) {
            try {
                return reserveInventory(req, customerId);
            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                if (++attempt >= maxRetry) {
                    throw new RoomUnavailableException("Could not secure room after " + maxRetry + " attempts");
                }
                log.warn("Optimistic lock clash on room {}, retry {}/{}", req.roomId(), attempt, maxRetry);
            }
        }
    }

    /**
     * Transactional core. Pessimistically locks the target inventory rows,
     * verifies none are booked, flips them, and creates the reservation in
     * PAYMENT_PENDING with an expiry the scheduler will reclaim if unpaid.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    protected Reservation reserveInventory(ReservationRequest req, Long customerId) {
        Room room = roomRepository.findById(req.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + req.roomId()));

        List<LocalDate> stayDates = datesBetween(req.checkIn(), req.checkOut());

        // Layer 2: pessimistic row locks over exactly the dates we need.
        List<RoomInventory> locked = inventoryRepository.lockForDates(req.roomId(), stayDates);

        // Ensure an inventory row exists for every requested date (lazy creation).
        List<RoomInventory> rows = ensureInventoryRows(req.roomId(), stayDates, locked);

        boolean anyBooked = rows.stream().anyMatch(RoomInventory::isBooked);
        if (anyBooked) {
            throw new RoomUnavailableException("Room already booked for one or more selected dates");
        }

        BigDecimal total = room.getPrice().multiply(BigDecimal.valueOf(stayDates.size()));
        Reservation reservation = reservationRepository.save(Reservation.builder()
                .customerId(customerId)
                .hotelId(room.getHotel().getId())
                .roomId(room.getId())
                .checkIn(req.checkIn())
                .checkOut(req.checkOut())
                .guests(req.guests())
                .totalAmount(total)
                .status(ReservationStatus.PAYMENT_PENDING)
                .expiresAt(Instant.now().plus(paymentWindowMinutes, ChronoUnit.MINUTES))
                .build());

        // Layer 3/4: flipping booked=true bumps @Version; unique (room,date) is the backstop.
        rows.forEach(r -> { r.setBooked(true); r.setReservationId(reservation.getId()); });
        inventoryRepository.saveAll(rows);

        eventPublisher.publish(KafkaTopicsConfig.RESERVATION_CREATED, reservation.getId().toString(),
                toEvent(reservation));
        eventPublisher.publish(KafkaTopicsConfig.INVENTORY_UPDATED, room.getId().toString(),
                new InventoryEvent(room.getId(), req.checkIn(), req.checkOut(), true, Instant.now()));
        return reservation;
    }

    /** Charge the gateway; CONFIRMED on success, auto-release + CANCELLED on failure. */
    @Transactional
    protected ReservationResponse settlePayment(Reservation reservation, ReservationRequest req) {
        Payment payment = paymentService.charge(reservation.getId(), reservation.getTotalAmount(), req.gateway());
        Reservation managed = reservationRepository.findById(reservation.getId()).orElseThrow();
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            managed.setStatus(ReservationStatus.CONFIRMED);
            managed.setExpiresAt(null);
            eventPublisher.publish(KafkaTopicsConfig.RESERVATION_CONFIRMED, managed.getId().toString(), toEvent(managed));
        } else {
            releaseInventory(managed);
            managed.setStatus(ReservationStatus.CANCELLED);
            eventPublisher.publish(KafkaTopicsConfig.RESERVATION_CANCELLED, managed.getId().toString(), toEvent(managed));
        }
        return reservationMapper.toResponse(managed);
    }

    @Transactional
    public ReservationResponse cancel(Long reservationId, Long customerId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationId));
        if (!reservation.getCustomerId().equals(customerId)) {
            throw new RoomUnavailableException("Not your reservation");
        }
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            paymentService.refund(reservationId);
        }
        releaseInventory(reservation);
        reservation.setStatus(ReservationStatus.CANCELLED);
        eventPublisher.publish(KafkaTopicsConfig.RESERVATION_CANCELLED, reservationId.toString(), toEvent(reservation));
        return reservationMapper.toResponse(reservation);
    }

    /** Frees every inventory row held by a reservation (used on cancel/expiry). */
    @Transactional
    public void releaseInventory(Reservation reservation) {
        List<LocalDate> dates = datesBetween(reservation.getCheckIn(), reservation.getCheckOut());
        List<RoomInventory> rows = inventoryRepository.lockForDates(reservation.getRoomId(), dates);
        rows.stream()
                .filter(r -> reservation.getId().equals(r.getReservationId()))
                .forEach(r -> { r.setBooked(false); r.setReservationId(null); });
        inventoryRepository.saveAll(rows);
        eventPublisher.publish(KafkaTopicsConfig.INVENTORY_UPDATED, reservation.getRoomId().toString(),
                new InventoryEvent(reservation.getRoomId(), reservation.getCheckIn(), reservation.getCheckOut(), false, Instant.now()));
    }

    private List<RoomInventory> ensureInventoryRows(Long roomId, List<LocalDate> dates, List<RoomInventory> existing) {
        List<RoomInventory> rows = new ArrayList<>(existing);
        List<LocalDate> present = existing.stream().map(RoomInventory::getStayDate).toList();
        for (LocalDate date : dates) {
            if (!present.contains(date)) {
                rows.add(inventoryRepository.save(RoomInventory.builder()
                        .roomId(roomId).stayDate(date).booked(false).build()));
            }
        }
        return rows;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new RoomUnavailableException("check-out must be after check-in");
        }
    }

    /** Nightly stay dates are [checkIn, checkOut) — you don't occupy checkout night. */
    private List<LocalDate> datesBetween(LocalDate checkIn, LocalDate checkOut) {
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = checkIn; d.isBefore(checkOut); d = d.plusDays(1)) dates.add(d);
        return dates;
    }

    private ReservationEvent toEvent(Reservation r) {
        return new ReservationEvent(r.getId(), r.getCustomerId(), r.getHotelId(), r.getRoomId(), r.getStatus(), Instant.now());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> myReservations(Long customerId) {
        return reservationRepository.findByCustomerId(customerId).stream()
                .map(reservationMapper::toResponse).toList();
    }
}
