package com.hotel.reservation.dto.room;

import com.hotel.reservation.domain.RoomStatus;
import com.hotel.reservation.domain.RoomType;

import java.math.BigDecimal;
import java.util.Set;

public record RoomResponse(
        Long id,
        Long hotelId,
        String roomNumber,
        RoomType roomType,
        int capacity,
        BigDecimal price,
        RoomStatus status,
        Set<String> features
) {}
