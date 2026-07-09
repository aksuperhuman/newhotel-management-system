package com.hotelreservation.exception;

import org.springframework.http.HttpStatus;

public class RoomUnavailableException extends BusinessException {
    public RoomUnavailableException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
