package com.hotel.reservation.service;

import com.hotel.reservation.concurrency.DistributedLock;
import com.hotel.reservation.config.KafkaTopicConfig;
import com.hotel.reservation.domain.*;
import com.hotel.reservation.dto.reservation.ReservationRequest;
import com.hotel.reservation.dto.reservation.ReservationResponse;
import com.hotel.reservation.event.InventoryEvent;
import com.hotel.reservation.event.ReservationEvent;
import com.hotel.reservation.exception.ResourceNotFoundException;
import com.hotel.reservation.exception.RoomUnavailableException;
import com.hotel.reservation.kafka.EventPublisher;
import com.hotel.reservation.mapper.ReservationMapper;
import com.hotel.reservation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ==========================================================================
 * HIGH-CONCURRENCY RESERVATION ENGINE
 * ==========================================================================
 * The booking path defends against double-booking with FOUR layers, cheapest
 * to strongest:
 *
 *  1. REDIS DISTRIBUTED LOCK (cross-instance gate)
 *     A short-lived lock keyed by room repels competing requests across all
 *     app instances before they touch the DB. Absorbs the thundering herd
 *     and keeps DB contention low.
 *
 *  2. PESSIMISTIC DB LOCK (SELECT ... FOR UPDATE)
 *     Inside the transaction we lock the room + its inventory rows so no
 *     other transaction on the SAME database can read-then-write them
 *     concurrently. This is the authoritative serialization point.
 *
 *  3. OPTIMISTIC LOCK (@Version) + RETRY
 *     Inventory/Reservation rows carry a @Version. If a race still slips
 *     through (e.g. lock TTL expiry), the version check fails the commit and
 *     the caller retries via createReservationWithRetry().
 *
 *  4. UNIQUE CONSTRAINT (room_id, stay_date)
 *     The database itself will reject a duplicate hold as the final backstop.
 *
 * Isolation is REPEATABLE_READ so the availability we read cannot change
 * underneath us within the transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final RoomInventoryRepository inventoryRepository;
    private final PaymentService paymentService;
    private final DistributedLock distributedLock;
    private final EventPublisher eventPublisher;
    private final ReservationMapper reservationMapper;

    @Value("${app.reservation.lock-ttl-seconds}")
    private long lockTtlSeconds;
    @Value("${app.reservation.hold-expiry-minutes}")
    private long holdExpiryMinutes;
    @Value("${app.reservation.max-retry-attempts}")
    private int maxRetryAttempts;

    /**
     * Public entrypoint. Wraps the transactional core with an optimistic-lock
     * retry loop (layer 3). We retry OUTSIDE the transaction so each attempt
     * gets a fresh persistence context.
     */
    public ReservationResponse book(Long userId, ReservationRequest request) {
        validateDates(request.checkIn(), request.checkOut());
        int attempt = 0;
        while (true) {
            try {
                return doBook(userId, request);
            } catch (org.springframework.dao.OptimisticLockingFailureException ex) {
                if (++attempt >= maxRetryAttempts) {
                    throw new RoomUnavailableException(
                        "Could not secure the room after " + attempt + " attempts, please retry");
                }
                backoff(attempt);
            }
        }
    }

    private ReservationResponse doBook(Long userId, ReservationRequest request) {
        // ---- Layer 1: Redis distributed lock ----
        String lockKey = DistributedLock.roomLockKey(request.roomId());
        String token = distributedLock.tryAcquire(lockKey, Duration.ofSeconds(lockTtlSeconds));
        if (token == null) {
            throw new RoomUnavailableException("Room is being booked by another user, please retry");
        }
        try {
            return reserveTransactional(userId, request);
        } finally {
            distributedLock.release(lockKey, token);
        }
    }

    /**
     * The transactional critical section. REPEATABLE_READ + pessimistic locks
     * on room and inventory give us ACID guarantees against concurrent writers.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    protected ReservationResponse reserveTransactional(Long userId, ReservationRequest request) {
        // Layer 2: pessimistic lock on the room row.
        Room room = roomRepository.findByIdForUpdate(request.roomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + request.roomId()));
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new RoomUnavailableException("Room is not available for booking");
        }

        List<LocalDate> nights = nightsBetween(request.checkIn(), request.checkOut());

        // Layer 2 (cont.): lock existing inventory rows for the range.
        List<RoomInventory> locked = inventoryRepository.findForUpdate(
            room.getId(), request.checkIn(), request.checkOut());
        boolean anyReserved = locked.stream().anyMatch(RoomInventory::isReserved);
        if (anyReserved) {
            throw new RoomUnavailableException("One or more nights are already booked");
        }

        BigDecimal total = room.getPrice().multiply(BigDecimal.valueOf(nights.size()));
        Reservation reservation = Reservation.builder()
            .reference(UUID.randomUUID().toString())
            .userId(userId)
            .roomId(room.getId())
            .checkIn(request.checkIn())
            .checkOut(request.checkOut())
            .guests(request.guests())
            .totalAmount(total)
            .status(ReservationStatus.PAYMENT_PENDING)
            .expiresAt(OffsetDateTime.now().plusMinutes(holdExpiryMinutes))
            .build();
        reservation = reservationRepository.save(reservation);

        // Write/flip inventory rows. Missing rows are created; the unique
        // constraint (layer 4) rejects any concurrent duplicate insert.
        upsertInventory(room.getId(), nights, reservation.getId(), locked);

        eventPublisher.publish(KafkaTopicConfig.RESERVATION_CREATED, reservation.getReference(),
            ReservationEvent.of("ReservationCreated", reservation.getId(), reservation.getReference(),
                userId, room.getId(), total));
        eventPublisher.publish(KafkaTopicConfig.INVENTORY_UPDATED, room.getId().toString(),
            InventoryEvent.of(room.getId(), "RESERVED", nights.size()));

        // Payment (with its own retry). Success confirms; failure releases.
        Payment payment = paymentService.charge(reservation, request.paymentProvider());
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setExpiresAt(null);
            reservationRepository.save(reservation);
            eventPublisher.publish(KafkaTopicConfig.RESERVATION_CONFIRMED, reservation.getReference(),
                ReservationEvent.of("ReservationConfirmed", reservation.getId(), reservation.getReference(),
                    userId, room.getId(), total));
        } else {
            releaseInventory(reservation);
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            throw new RoomUnavailableException("Payment failed, reservation cancelled");
        }
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public ReservationResponse cancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationId));
        if (reservation.getStatus() == ReservationStatus.CHECKED_IN
                || reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new RoomUnavailableException("Cannot cancel a checked-in or completed reservation");
        }
        releaseInventory(reservation);
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        eventPublisher.publish(KafkaTopicConfig.RESERVATION_CANCELLED, reservation.getReference(),
            ReservationEvent.of("ReservationCancelled", reservation.getId(), reservation.getReference(),
                reservation.getUserId(), reservation.getRoomId(), reservation.getTotalAmount()));
        return reservationMapper.toResponse(reservation);
    }

    /** Releases every inventory night held by a reservation. */
    @Transactional
    public void releaseInventory(Reservation reservation) {
        List<RoomInventory> held = inventoryRepository.findByReservationId(reservation.getId());
        for (RoomInventory inv : held) {
            inv.setReserved(false);
            inv.setReservationId(null);
        }
        inventoryRepository.saveAll(held);
        eventPublisher.publish(KafkaTopicConfig.INVENTORY_UPDATED, reservation.getRoomId().toString(),
            InventoryEvent.of(reservation.getRoomId(), "RELEASED", held.size()));
    }

    private void upsertInventory(Long roomId, List<LocalDate> nights, Long reservationId,
                                 List<RoomInventory> existing) {
        List<RoomInventory> toSave = new ArrayList<>();
        for (LocalDate night : nights) {
            RoomInventory row = existing.stream()
                .filter(i -> i.getStayDate().equals(night))
                .findFirst()
                .orElseGet(() -> RoomInventory.builder().roomId(roomId).stayDate(night).build());
            row.setReserved(true);
            row.setReservationId(reservationId);
            toSave.add(row);
        }
        inventoryRepository.saveAll(toSave);
    }

    private List<LocalDate> nightsBetween(LocalDate checkIn, LocalDate checkOut) {
        List<LocalDate> nights = new ArrayList<>();
        for (LocalDate d = checkIn; d.isBefore(checkOut); d = d.plusDays(1)) {
            nights.add(d);
        }
        return nights;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new RoomUnavailableException("check-out must be after check-in");
        }
        if (ChronoUnit.DAYS.between(checkIn, checkOut) > 30) {
            throw new RoomUnavailableException("Stay cannot exceed 30 nights");
        }
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(50L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Transactional(readOnly = true)
    public List<Reservation> findExpiredHolds() {
        return reservationRepository.findExpiredHolds(ReservationStatus.PAYMENT_PENDING, OffsetDateTime.now());
    }
}
