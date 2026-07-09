package com.hotelreservation.event;

import java.time.Instant;
import java.time.LocalDate;

public record InventoryEvent(
        Long roomId,
        LocalDate checkIn,
        LocalDate checkOut,
        boolean booked,
        Instant occurredAt
) implements DomainEvent {}
