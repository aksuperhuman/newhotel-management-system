package com.hotelreservation.event;

import java.time.Instant;

/** Marker for all domain events published to Kafka. */
public interface DomainEvent {
    Instant occurredAt();
}
