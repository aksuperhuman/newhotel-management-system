package com.hotelreservation.repository;

import com.hotelreservation.domain.Reservation;
import com.hotelreservation.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByCustomerId(Long customerId);

    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant cutoff);

    long countByStatus(ReservationStatus status);

    List<Reservation> findByStatusIn(List<ReservationStatus> statuses);
}
