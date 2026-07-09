package com.hotelreservation.notification;

/** Observer contract: each channel reacts to a notification message. */
public interface NotificationChannel {
    void send(String message);
    String name();
}
