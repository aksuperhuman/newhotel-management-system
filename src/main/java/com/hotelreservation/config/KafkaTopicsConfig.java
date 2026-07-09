package com.hotelreservation.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    public static final String RESERVATION_CREATED   = "reservation.created";
    public static final String RESERVATION_CONFIRMED = "reservation.confirmed";
    public static final String RESERVATION_CANCELLED = "reservation.cancelled";
    public static final String PAYMENT_COMPLETED     = "payment.completed";
    public static final String PAYMENT_FAILED        = "payment.failed";
    public static final String INVENTORY_UPDATED     = "inventory.updated";

    @Bean NewTopic reservationCreated()   { return TopicBuilder.name(RESERVATION_CREATED).partitions(3).replicas(1).build(); }
    @Bean NewTopic reservationConfirmed() { return TopicBuilder.name(RESERVATION_CONFIRMED).partitions(3).replicas(1).build(); }
    @Bean NewTopic reservationCancelled() { return TopicBuilder.name(RESERVATION_CANCELLED).partitions(3).replicas(1).build(); }
    @Bean NewTopic paymentCompleted()     { return TopicBuilder.name(PAYMENT_COMPLETED).partitions(3).replicas(1).build(); }
    @Bean NewTopic paymentFailed()        { return TopicBuilder.name(PAYMENT_FAILED).partitions(3).replicas(1).build(); }
    @Bean NewTopic inventoryUpdated()     { return TopicBuilder.name(INVENTORY_UPDATED).partitions(3).replicas(1).build(); }
}
