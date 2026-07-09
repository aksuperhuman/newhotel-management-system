package com.hotel.reservation.repository;

import com.hotel.reservation.domain.RoomInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {

    /**
     * Pessimistic read of the inventory rows for a room across a date range.
     * SELECT ... FOR UPDATE ensures that once we've inspected availability,
     * no concurrent transaction can flip these rows before we commit.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from RoomInventory i where i.roomId = :roomId " +
           "and i.stayDate >= :start and i.stayDate < :end")
    List<RoomInventory> findForUpdate(@Param("roomId") Long roomId,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);

    List<RoomInventory> findByRoomIdAndStayDateBetween(Long roomId, LocalDate start, LocalDate end);

    List<RoomInventory> findByReservationId(Long reservationId);
}
