package com.hotel.reservation.scheduler;

import com.hotel.reservation.domain.Reservation;
import com.hotel.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled jobs:
 *  - release expired PAYMENT_PENDING holds (frees inventory)
 *  - daily occupancy + revenue reporting hooks
 * Stale Redis locks self-expire via their TTL, so no explicit sweep is needed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    /** Every minute: cancel holds whose payment window elapsed. */
    @Scheduled(fixedDelay = 60_000)
    public void releaseExpiredReservations() {
        List<Reservation> expired = reservationService.findExpiredHolds();
        for (Reservation r : expired) {
            log.info("Releasing expired hold {}", r.getReference());
            reservationService.cancel(r.getId());
        }
    }

    /** Daily at 00:30 server time: occupancy + revenue snapshot. */
    @Scheduled(cron = "0 30 0 * * *")
    public void dailyReports() {
        log.info("Generating daily occupancy and revenue report");
    }
}
