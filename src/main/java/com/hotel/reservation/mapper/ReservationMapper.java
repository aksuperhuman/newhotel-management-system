package com.hotel.reservation.mapper;

import com.hotel.reservation.domain.Reservation;
import com.hotel.reservation.dto.reservation.ReservationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    ReservationResponse toResponse(Reservation reservation);
}
