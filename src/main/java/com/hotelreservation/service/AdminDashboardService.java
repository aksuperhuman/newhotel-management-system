package com.hotelreservation.service;

import com.hotelreservation.domain.enums.PaymentStatus;
import com.hotelreservation.domain.enums.ReservationStatus;
import com.hotelreservation.dto.admin.DashboardResponse;
import com.hotelreservation.repository.HotelRepository;
import com.hotelreservation.repository.PaymentRepository;
import com.hotelreservation.repository.ReservationRepository;
import com.hotelreservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        long totalHotels = hotelRepository.count();
        long totalRooms = roomRepository.count();
        long active = reservationRepository.countByStatus(ReservationStatus.CONFIRMED)
                + reservationRepository.countByStatus(ReservationStatus.CHECKED_IN);
        long cancelled = reservationRepository.countByStatus(ReservationStatus.CANCELLED);
        long allReservations = reservationRepository.count();

        BigDecimal revenue = paymentRepository.findByStatus(PaymentStatus.SUCCESS).stream()
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double cancellationRate = allReservations == 0 ? 0.0
                : BigDecimal.valueOf(cancelled).divide(BigDecimal.valueOf(allReservations), 4, RoundingMode.HALF_UP).doubleValue();
        double occupancyRate = totalRooms == 0 ? 0.0
                : BigDecimal.valueOf(active).divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP).doubleValue();

        return new DashboardResponse(totalHotels, totalRooms, active, revenue,
                cancellationRate, occupancyRate, List.of());
    }
}
