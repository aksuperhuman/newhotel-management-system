package com.hotel.reservation.dto.hotel;

import java.util.List;
import java.util.Set;

public record HotelResponse(
        Long id,
        String name,
        String description,
        String city,
        String address,
        int starRating,
        Long managerId,
        Set<String> amenities,
        List<String> images
) {}
