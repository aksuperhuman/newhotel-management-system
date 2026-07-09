package com.hotel.reservation.event;

import java.time.OffsetDateTime;

/** Marker + common metadata for all published domain events. */
public interface DomainEvent {
    String eventId();
    OffsetDateTime occurredAt();
}
