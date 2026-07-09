package com.hotelreservation.domain.enums;

/**
 * Reservation lifecycle:
 * PENDING -> LOCK_ROOM -> PAYMENT_PENDING -> CONFIRMED -> CHECKED_IN -> CHECKED_OUT -> COMPLETED
 * Any pre-CONFIRMED state may transition to CANCELLED, which releases inventory.
 */
public enum ReservationStatus {
    PENDING,
    LOCK_ROOM,
    PAYMENT_PENDING,
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT,
    COMPLETED,
    CANCELLED
}
