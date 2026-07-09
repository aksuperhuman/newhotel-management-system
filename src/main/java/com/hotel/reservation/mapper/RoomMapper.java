package com.hotel.reservation.mapper;

import com.hotel.reservation.domain.Room;
import com.hotel.reservation.dto.room.RoomRequest;
import com.hotel.reservation.dto.room.RoomResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    RoomResponse toResponse(Room room);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotelId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    Room toEntity(RoomRequest request);
}
