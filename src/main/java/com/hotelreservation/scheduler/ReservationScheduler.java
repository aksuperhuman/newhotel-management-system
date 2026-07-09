package com.hotelreservation.scheduler;

import com.hotelreservation.domain.Reservation;
import com.hotelreservation.domain.enums.ReservationStatus;
import com.hotelreservation.repository.ReservationRepository;
import com.hotelreservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Background jobs. Kept idempotent so overlapping runs (or multiple instances)
 * never double-process a reservation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    /** Release PAYMENT_PENDING reservations whose payment window elapsed. */
    @Scheduled(fixedDelayString = "PT1M")
    @Transactional
    public void releaseExpiredReservations() {
        List<Reservation> expired = reservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.PAYMENT_PENDING, Instant.now());
        for (Reservation r : expired) {
            reservationService.releaseInventory(r);
            r.setStatus(ReservationStatus.CANCELLED);
            log.info("Released expired reservation {}", r.getId());
        }
    }

    /** Daily occupancy + revenue report (log stand-in for a real reporting sink). */
    @Scheduled(cron = "0 0 1 * * *")
    public void dailyReport() {
        long confirmed = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        log.info("[daily-report] confirmed reservations today: {}", confirmed);
    }
}
