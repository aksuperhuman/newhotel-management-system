package com.hotelreservation.dto.search;

import com.hotelreservation.domain.enums.RoomType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Search criteria. Null fields are ignored so callers can mix and match filters.
 */
public record HotelSearchRequest(
        String city,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guests,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer starRating,
        Set<Long> amenityIds,
        RoomType roomType
) {}
