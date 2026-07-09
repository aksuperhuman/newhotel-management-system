package com.hotel.reservation.service;

import com.hotel.reservation.domain.Hotel;
import com.hotel.reservation.dto.hotel.HotelRequest;
import com.hotel.reservation.dto.hotel.HotelResponse;
import com.hotel.reservation.exception.ResourceNotFoundException;
import com.hotel.reservation.mapper.HotelMapper;
import com.hotel.reservation.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    @Transactional
    public HotelResponse create(HotelRequest request, Long managerId) {
        Hotel hotel = hotelMapper.toEntity(request);
        hotel.setManagerId(managerId);
        return hotelMapper.toResponse(hotelRepository.save(hotel));
    }

    @Transactional
    @CacheEvict(value = "hotels", key = "#id")
    public HotelResponse update(Long id, HotelRequest request) {
        Hotel hotel = getOrThrow(id);
        hotelMapper.update(request, hotel);
        return hotelMapper.toResponse(hotelRepository.save(hotel));
    }

    @Transactional
    @CacheEvict(value = "hotels", key = "#id")
    public void delete(Long id) {
        hotelRepository.delete(getOrThrow(id));
    }

    @Cacheable(value = "hotels", key = "#id")
    @Transactional(readOnly = true)
    public HotelResponse get(Long id) {
        return hotelMapper.toResponse(getOrThrow(id));
    }

    private Hotel getOrThrow(Long id) {
        return hotelRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
    }
}
