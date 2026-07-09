package com.hotel.reservation.service;

import com.hotel.reservation.domain.PaymentStatus;
import com.hotel.reservation.domain.ReservationStatus;
import com.hotel.reservation.dto.admin.DashboardStats;
import com.hotel.reservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final RoomInventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public DashboardStats stats() {
        long totalHotels = hotelRepository.count();
        long totalRooms = roomRepository.count();
        long active = reservationRepository.countByStatus(ReservationStatus.CONFIRMED)
                + reservationRepository.countByStatus(ReservationStatus.CHECKED_IN);
        long cancelled = reservationRepository.countByStatus(ReservationStatus.CANCELLED);
        long allReservations = reservationRepository.count();

        BigDecimal revenue = paymentRepository.findAll().stream()
            .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
            .map(p -> p.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        double cancellationRate = allReservations == 0 ? 0.0
            : round((double) cancelled / allReservations * 100);

        long reservedNights = inventoryRepository.findAll().stream()
            .filter(i -> i.isReserved()).count();
        double occupancyRate = totalRooms == 0 ? 0.0
            : round((double) reservedNights / (totalRooms * 30) * 100);

        List<Map<String, Object>> mostBooked = List.of(); // populated via a projection query in production

        return new DashboardStats(totalHotels, totalRooms, active, revenue,
            cancellationRate, occupancyRate, mostBooked);
    }

    private double round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
