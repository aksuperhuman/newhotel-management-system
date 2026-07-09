package com.hotelreservation.service;

import com.hotelreservation.domain.Hotel;
import com.hotelreservation.domain.Room;
import com.hotelreservation.dto.room.RoomRequest;
import com.hotelreservation.dto.room.RoomResponse;
import com.hotelreservation.exception.ResourceNotFoundException;
import com.hotelreservation.mapper.RoomMapper;
import com.hotelreservation.repository.HotelRepository;
import com.hotelreservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    @Transactional
    public RoomResponse addRoom(Long hotelId, RoomRequest req) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + hotelId));
        Room room = Room.builder()
                .hotel(hotel)
                .roomNumber(req.roomNumber())
                .roomType(req.roomType())
                .capacity(req.capacity())
                .price(req.price())
                .features(req.features())
                .build();
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> listByHotel(Long hotelId) {
        return roomRepository.findByHotelId(hotelId).stream().map(roomMapper::toResponse).toList();
    }
}
