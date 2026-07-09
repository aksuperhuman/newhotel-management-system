package com.hotelreservation.event;

import com.hotelreservation.domain.enums.PaymentGateway;
import com.hotelreservation.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentEvent(
        Long paymentId,
        Long reservationId,
        PaymentGateway gateway,
        BigDecimal amount,
        PaymentStatus status,
        Instant occurredAt
) implements DomainEvent {}
