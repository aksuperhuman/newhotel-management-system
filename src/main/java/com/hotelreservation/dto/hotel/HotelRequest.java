package com.hotelreservation.dto.hotel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record HotelRequest(
        @NotBlank String name,
        String description,
        @NotBlank String city,
        String address,
        @Min(0) @Max(5) int starRating,
        Set<Long> amenityIds
) {}
