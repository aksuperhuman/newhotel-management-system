package com.hotel.reservation.dto.reservation;

import com.hotel.reservation.domain.PaymentProvider;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequest(
        @NotNull Long roomId,
        @NotNull @Future LocalDate checkIn,
        @NotNull @Future LocalDate checkOut,
        @Min(1) int guests,
        @NotNull PaymentProvider paymentProvider
) {}
