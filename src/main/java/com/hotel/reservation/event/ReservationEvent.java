package com.hotel.reservation.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Emitted for ReservationCreated / Confirmed / Cancelled. The `type` field
 * lets a single record class serve all three topics.
 */
public record ReservationEvent(
        String eventId,
        OffsetDateTime occurredAt,
        String type,
        Long reservationId,
        String reference,
        Long userId,
        Long roomId,
        BigDecimal totalAmount
) implements DomainEvent {

    public static ReservationEvent of(String type, Long reservationId, String reference,
                                      Long userId, Long roomId, BigDecimal amount) {
        return new ReservationEvent(UUID.randomUUID().toString(), OffsetDateTime.now(),
                type, reservationId, reference, userId, roomId, amount);
    }
}
