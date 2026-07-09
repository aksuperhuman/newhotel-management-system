package com.hotelreservation.event;

import com.hotelreservation.domain.enums.ReservationStatus;

import java.time.Instant;

public record ReservationEvent(
        Long reservationId,
        Long customerId,
        Long hotelId,
        Long roomId,
        ReservationStatus status,
        Instant occurredAt
) implements DomainEvent {}
