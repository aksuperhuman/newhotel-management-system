package com.hotelreservation.dto.hotel;

import java.util.Set;

public record HotelResponse(
        Long id,
        String name,
        String description,
        String city,
        String address,
        int starRating,
        boolean active,
        Set<String> imageUrls,
        Set<Long> amenityIds
) {}
