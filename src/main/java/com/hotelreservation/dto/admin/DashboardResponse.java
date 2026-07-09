package com.hotelreservation.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        long totalHotels,
        long totalRooms,
        long activeReservations,
        BigDecimal totalRevenue,
        double cancellationRate,
        double occupancyRate,
        List<TopHotel> mostBookedHotels
) {
    public record TopHotel(Long hotelId, String name, long bookings) {}
}
