package com.hotelreservation.service;

import com.hotelreservation.domain.Hotel;
import com.hotelreservation.domain.User;
import com.hotelreservation.dto.hotel.HotelRequest;
import com.hotelreservation.dto.hotel.HotelResponse;
import com.hotelreservation.exception.ResourceNotFoundException;
import com.hotelreservation.mapper.HotelMapper;
import com.hotelreservation.repository.HotelRepository;
import com.hotelreservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final HotelMapper hotelMapper;

    @Transactional
    public HotelResponse create(HotelRequest req, String managerEmail) {
        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        Hotel hotel = Hotel.builder()
                .name(req.name())
                .description(req.description())
                .city(req.city())
                .address(req.address())
                .starRating(req.starRating())
                .manager(manager)
                .active(true)
                .amenityIds(req.amenityIds() == null ? new HashSet<>() : new HashSet<>(req.amenityIds()))
                .build();
        return hotelMapper.toResponse(hotelRepository.save(hotel));
    }

    @Cacheable(value = "hotels", key = "#id")
    @Transactional(readOnly = true)
    public HotelResponse get(Long id) {
        return hotelMapper.toResponse(find(id));
    }

    @CacheEvict(value = "hotels", key = "#id")
    @Transactional
    public HotelResponse update(Long id, HotelRequest req) {
        Hotel hotel = find(id);
        hotel.setName(req.name());
        hotel.setDescription(req.description());
        hotel.setCity(req.city());
        hotel.setAddress(req.address());
        hotel.setStarRating(req.starRating());
        if (req.amenityIds() != null) hotel.setAmenityIds(new HashSet<>(req.amenityIds()));
        return hotelMapper.toResponse(hotel);
    }

    @CacheEvict(value = "hotels", key = "#id")
    @Transactional
    public void delete(Long id) {
        Hotel hotel = find(id);
        hotel.setActive(false); // soft delete keeps historical reservations intact
    }

    @CacheEvict(value = "hotels", key = "#id")
    @Transactional
    public void addImage(Long id, String url) {
        find(id).getImageUrls().add(url);
    }

    private Hotel find(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
    }
}
