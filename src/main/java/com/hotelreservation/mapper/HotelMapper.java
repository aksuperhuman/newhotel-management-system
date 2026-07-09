package com.hotelreservation.mapper;

import com.hotelreservation.domain.Hotel;
import com.hotelreservation.dto.hotel.HotelResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HotelMapper {
    HotelResponse toResponse(Hotel hotel);
}
