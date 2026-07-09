package com.hotel.reservation.mapper;

import com.hotel.reservation.domain.Hotel;
import com.hotel.reservation.dto.hotel.HotelRequest;
import com.hotel.reservation.dto.hotel.HotelResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HotelMapper {

    HotelResponse toResponse(Hotel hotel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "managerId", ignore = true)
    @Mapping(target = "images", ignore = true)
    Hotel toEntity(HotelRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "managerId", ignore = true)
    @Mapping(target = "images", ignore = true)
    void update(HotelRequest request, @MappingTarget Hotel hotel);
}
