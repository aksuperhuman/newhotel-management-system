package com.hotelreservation.repository;

import com.hotelreservation.domain.Payment;
import com.hotelreservation.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservationId(Long reservationId);
    List<Payment> findByStatus(PaymentStatus status);
}
