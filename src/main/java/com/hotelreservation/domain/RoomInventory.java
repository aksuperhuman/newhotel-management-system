package com.hotelreservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * One row per (room, date). This is the true source of availability and the
 * unit of concurrency control. Booking a stay = flipping `booked` to true for
 * every date in the range, guarded by locking + the (room_id, stay_date)
 * unique constraint as a last-line defense against double booking.
 */
@Entity
@Table(name = "room_inventory",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "stay_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "stay_date", nullable = false)
    private LocalDate stayDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean booked = false;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Version
    private Long version;
}
