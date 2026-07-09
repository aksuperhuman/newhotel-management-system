package com.hotel.reservation.dto.search;

import com.hotel.reservation.domain.RoomType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Search filters. Nullable fields are simply skipped when building the JPA
 * Specification, so any combination is valid.
 */
public record HotelSearchCriteria(
        String city,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guests,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer starRating,
        Set<String> amenities,
        RoomType roomType
) {}
