package com.hotelreservation.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Thin wrapper around KafkaTemplate so services publish events without knowing Kafka details. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String key, Object event) {
        log.info("Publishing event to topic={} key={}", topic, key);
        kafkaTemplate.send(topic, key, event);
    }
}
