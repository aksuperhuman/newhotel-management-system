package com.hotelreservation.event;

import com.hotelreservation.config.KafkaTopicsConfig;
import com.hotelreservation.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Bridges Kafka events to the notification (Observer) layer. */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopicsConfig.RESERVATION_CREATED, groupId = "notifications")
    public void onCreated(ReservationEvent e) {
        notificationService.notifyAll("Booking created for reservation #" + e.reservationId());
    }

    @KafkaListener(topics = KafkaTopicsConfig.RESERVATION_CANCELLED, groupId = "notifications")
    public void onCancelled(ReservationEvent e) {
        notificationService.notifyAll("Booking cancelled for reservation #" + e.reservationId());
    }

    @KafkaListener(topics = KafkaTopicsConfig.PAYMENT_COMPLETED, groupId = "notifications")
    public void onPaymentCompleted(PaymentEvent e) {
        notificationService.notifyAll("Payment success for reservation #" + e.reservationId());
    }
}
