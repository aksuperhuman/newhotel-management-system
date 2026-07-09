package com.hotelreservation.repository;

import com.hotelreservation.domain.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelId(Long hotelId);

    long countByHotelId(Long hotelId);

    /**
     * PESSIMISTIC_WRITE takes a DB row lock (SELECT ... FOR UPDATE). Any other
     * transaction trying to lock the same room blocks until this one commits.
     * Used as the strong-consistency path when we must serialize access to a
     * single hot room, at the cost of throughput.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);
}
