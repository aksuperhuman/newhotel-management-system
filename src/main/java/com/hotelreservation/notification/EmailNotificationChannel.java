package com.hotelreservation.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationChannel implements NotificationChannel {
    @Override public void send(String message) { log.info("[EMAIL] {}", message); }
    @Override public String name() { return "EMAIL"; }
}
