package com.hotel.reservation.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * One row per room per night. The (room_id, stay_date) unique constraint is
 * the last line of defense against double booking; the @Version field lets us
 * detect concurrent writes with optimistic locking.
 */
@Entity
@Table(name = "room_inventory", uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "stay_date"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "stay_date", nullable = false)
    private LocalDate stayDate;

    @Column(nullable = false)
    private boolean reserved;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Version
    private Long version;
}
