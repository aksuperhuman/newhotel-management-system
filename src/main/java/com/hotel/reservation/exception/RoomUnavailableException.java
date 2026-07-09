package com.hotel.reservation.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a room cannot be held for the requested dates (double-booking guard). */
public class RoomUnavailableException extends BusinessException {
    public RoomUnavailableException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
