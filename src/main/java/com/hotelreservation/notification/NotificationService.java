package com.hotelreservation.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Observer pattern: the subject holds a list of channels (observers) injected by
 * Spring, and fans a message out to all of them. Adding a new channel (e.g. push
 * notifications) requires zero changes here.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final List<NotificationChannel> channels;

    public void notifyAll(String message) {
        channels.forEach(c -> c.send(message));
    }
}
