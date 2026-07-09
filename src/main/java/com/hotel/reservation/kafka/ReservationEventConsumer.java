package com.hotel.reservation.kafka;

import com.hotel.reservation.config.KafkaTopicConfig;
import com.hotel.reservation.event.PaymentEvent;
import com.hotel.reservation.event.ReservationEvent;
import com.hotel.reservation.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumers act as the Observer side of the event system: they react to domain
 * events to drive notifications and analytics, fully decoupled from the
 * services that publish them.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopicConfig.RESERVATION_CREATED, groupId = "notification-service")
    public void onReservationCreated(ReservationEvent event) {
        log.info("[analytics] reservation created: {}", event.reference());
        notificationService.notifyBookingCreated(event);
    }

    @KafkaListener(topics = KafkaTopicConfig.RESERVATION_CONFIRMED, groupId = "notification-service")
    public void onReservationConfirmed(ReservationEvent event) {
        notificationService.notifyBookingConfirmed(event);
    }

    @KafkaListener(topics = KafkaTopicConfig.RESERVATION_CANCELLED, groupId = "notification-service")
    public void onReservationCancelled(ReservationEvent event) {
        notificationService.notifyBookingCancelled(event);
    }

    @KafkaListener(topics = KafkaTopicConfig.PAYMENT_COMPLETED, groupId = "notification-service")
    public void onPaymentCompleted(PaymentEvent event) {
        notificationService.notifyPaymentSuccess(event);
    }

    @KafkaListener(topics = KafkaTopicConfig.PAYMENT_FAILED, groupId = "notification-service")
    public void onPaymentFailed(PaymentEvent event) {
        log.warn("[analytics] payment failed for reservation {}", event.reservationId());
    }
}
