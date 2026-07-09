package com.hotel.reservation.dto.reservation;

import com.hotel.reservation.domain.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ReservationResponse(
        Long id,
        String reference,
        Long userId,
        Long roomId,
        LocalDate checkIn,
        LocalDate checkOut,
        int guests,
        BigDecimal totalAmount,
        ReservationStatus status,
        OffsetDateTime expiresAt
) {}
