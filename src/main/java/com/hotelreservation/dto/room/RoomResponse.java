package com.hotelreservation.dto.room;

import com.hotelreservation.domain.enums.RoomStatus;
import com.hotelreservation.domain.enums.RoomType;

import java.math.BigDecimal;

public record RoomResponse(
        Long id,
        Long hotelId,
        String roomNumber,
        RoomType roomType,
        int capacity,
        BigDecimal price,
        RoomStatus status,
        String features
) {}
