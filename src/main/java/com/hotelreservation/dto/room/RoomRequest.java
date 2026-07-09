package com.hotelreservation.dto.room;

import com.hotelreservation.domain.enums.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank String roomNumber,
        @NotNull RoomType roomType,
        @Min(1) int capacity,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        String features
) {}
