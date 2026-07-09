package com.hotelreservation.mapper;

import com.hotelreservation.domain.Room;
import com.hotelreservation.dto.room.RoomResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(target = "hotelId", source = "hotel.id")
    RoomResponse toResponse(Room room);
}
