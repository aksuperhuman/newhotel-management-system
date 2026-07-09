package com.hotelreservation.repository;

import com.hotelreservation.domain.RoomInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {

    List<RoomInventory> findByRoomIdAndStayDateBetween(Long roomId, LocalDate start, LocalDate end);

    /**
     * Pessimistic row locks over the exact set of (room, date) inventory rows a
     * booking needs. Locking every target date in one query prevents two
     * bookings from interleaving on overlapping ranges.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from RoomInventory i where i.roomId = :roomId and i.stayDate in :dates")
    List<RoomInventory> lockForDates(@Param("roomId") Long roomId, @Param("dates") List<LocalDate> dates);

    @Query("select case when count(i) > 0 then true else false end " +
           "from RoomInventory i where i.roomId = :roomId and i.stayDate in :dates and i.booked = true")
    boolean existsBookedForDates(@Param("roomId") Long roomId, @Param("dates") List<LocalDate> dates);
}
