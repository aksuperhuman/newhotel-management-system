package com.hotel.reservation.domain;

/**
 * Reservation lifecycle:
 * PENDING -> LOCK_ROOM -> PAYMENT_PENDING -> CONFIRMED
 *   -> CHECKED_IN -> CHECKED_OUT -> COMPLETED
 * CANCELLED is a terminal state reachable from any pre-CHECKED_IN state and
 * triggers automatic inventory release.
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
