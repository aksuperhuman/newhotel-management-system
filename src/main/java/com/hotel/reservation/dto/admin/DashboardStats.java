package com.hotel.reservation.dto.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardStats(
        long totalHotels,
        long totalRooms,
        long activeReservations,
        BigDecimal totalRevenue,
        double cancellationRate,
        double occupancyRate,
        List<Map<String, Object>> mostBookedHotels
) {}
