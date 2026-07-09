package com.hotelreservation.mapper;

import com.hotelreservation.domain.Reservation;
import com.hotelreservation.dto.reservation.ReservationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    ReservationResponse toResponse(Reservation reservation);
}
