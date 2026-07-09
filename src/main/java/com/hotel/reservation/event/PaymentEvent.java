package com.hotel.reservation.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentEvent(
        String eventId,
        OffsetDateTime occurredAt,
        String type,
        Long paymentId,
        Long reservationId,
        String provider,
        BigDecimal amount,
        boolean success
) implements DomainEvent {

    public static PaymentEvent of(String type, Long paymentId, Long reservationId,
                                  String provider, BigDecimal amount, boolean success) {
        return new PaymentEvent(UUID.randomUUID().toString(), OffsetDateTime.now(),
                type, paymentId, reservationId, provider, amount, success);
    }
}
