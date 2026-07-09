package com.hotel.reservation.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InventoryEvent(
        String eventId,
        OffsetDateTime occurredAt,
        Long roomId,
        String action,   // RESERVED | RELEASED
        int nights
) implements DomainEvent {

    public static InventoryEvent of(Long roomId, String action, int nights) {
        return new InventoryEvent(UUID.randomUUID().toString(), OffsetDateTime.now(), roomId, action, nights);
    }
}
