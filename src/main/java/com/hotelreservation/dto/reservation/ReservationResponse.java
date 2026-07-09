package com.hotelreservation.dto.reservation;

import com.hotelreservation.domain.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        Long customerId,
        Long hotelId,
        Long roomId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guests,
        BigDecimal totalAmount,
        ReservationStatus status,
        Instant expiresAt
) {}
