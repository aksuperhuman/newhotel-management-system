package com.hotel.reservation.service;

import com.hotel.reservation.event.PaymentEvent;
import com.hotel.reservation.event.ReservationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Simulated notification fan-out over Email + SMS channels. */
@Slf4j
@Service
public class NotificationService {

    public void notifyBookingCreated(ReservationEvent e) {
        send("EMAIL", e.userId(), "Your booking " + e.reference() + " was created");
    }

    public void notifyBookingConfirmed(ReservationEvent e) {
        send("EMAIL", e.userId(), "Booking " + e.reference() + " confirmed");
        send("SMS", e.userId(), "Booking confirmed: " + e.reference());
    }

    public void notifyBookingCancelled(ReservationEvent e) {
        send("EMAIL", e.userId(), "Booking " + e.reference() + " was cancelled");
    }

    public void notifyPaymentSuccess(PaymentEvent e) {
        send("EMAIL", e.reservationId(), "Payment received: " + e.amount());
    }

    public void notifyRefundCompleted(PaymentEvent e) {
        send("EMAIL", e.reservationId(), "Refund completed: " + e.amount());
    }

    private void send(String channel, Long recipientRef, String message) {
        log.info("[{}] -> user/ref {}: {}", channel, recipientRef, message);
    }
}
