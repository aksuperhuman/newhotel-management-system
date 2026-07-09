package com.hotel.reservation.dto.room;

import com.hotel.reservation.domain.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Set;

public record RoomRequest(
        @NotBlank String roomNumber,
        @NotNull RoomType roomType,
        @Min(1) int capacity,
        @NotNull @Positive BigDecimal price,
        Set<String> features
) {}
