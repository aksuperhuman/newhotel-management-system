package com.hotel.reservation.kafka;

import com.hotel.reservation.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around KafkaTemplate. Centralizes publishing so services stay
 * decoupled from the messaging infrastructure (they depend on this port, not
 * on Kafka directly).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String key, DomainEvent event) {
        kafkaTemplate.send(topic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event {} to {}", event.eventId(), topic, ex);
                } else {
                    log.debug("Published event {} to {}", event.eventId(), topic);
                }
            });
    }
}
