package com.hotelreservation.event;

import com.hotelreservation.config.KafkaTopicsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

/**
 * Analytics service (Kafka consumer). In a real system this would write to a
 * warehouse / OLAP store; here it maintains in-memory counters as a stand-in.
 */
@Slf4j
@Component
public class AnalyticsConsumer {

    private final LongAdder confirmed = new LongAdder();
    private final LongAdder cancelled = new LongAdder();

    @KafkaListener(topics = KafkaTopicsConfig.RESERVATION_CONFIRMED, groupId = "analytics")
    public void onConfirmed(ReservationEvent event) {
        confirmed.increment();
        log.info("[analytics] reservation confirmed id={} total={}", event.reservationId(), confirmed.sum());
    }

    @KafkaListener(topics = KafkaTopicsConfig.RESERVATION_CANCELLED, groupId = "analytics")
    public void onCancelled(ReservationEvent event) {
        cancelled.increment();
        log.info("[analytics] reservation cancelled id={} total={}", event.reservationId(), cancelled.sum());
    }

    @KafkaListener(topics = KafkaTopicsConfig.INVENTORY_UPDATED, groupId = "analytics")
    public void onInventory(InventoryEvent event) {
        log.info("[analytics] inventory update room={} booked={}", event.roomId(), event.booked());
    }
}
