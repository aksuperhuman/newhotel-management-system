package com.hotel.reservation.repository;

import com.hotel.reservation.domain.Reservation;
import com.hotel.reservation.domain.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReference(String reference);

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    long countByStatus(ReservationStatus status);

    /** Reservations still awaiting payment whose hold window has lapsed. */
    @Query("select r from Reservation r where r.status = :status and r.expiresAt < :now")
    List<Reservation> findExpiredHolds(@Param("status") ReservationStatus status,
                                       @Param("now") OffsetDateTime now);
}
