package com.hotel.reservation.service;

import com.hotel.reservation.domain.Room;
import com.hotel.reservation.domain.RoomStatus;
import com.hotel.reservation.dto.room.RoomRequest;
import com.hotel.reservation.dto.room.RoomResponse;
import com.hotel.reservation.exception.ResourceNotFoundException;
import com.hotel.reservation.mapper.RoomMapper;
import com.hotel.reservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Transactional
    public RoomResponse addRoom(Long hotelId, RoomRequest request) {
        Room room = roomMapper.toEntity(request);
        room.setHotelId(hotelId);
        room.setStatus(RoomStatus.AVAILABLE);
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> listByHotel(Long hotelId) {
        return roomRepository.findByHotelId(hotelId).stream()
            .map(roomMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse get(Long id) {
        return roomMapper.toResponse(roomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id)));
    }
}
