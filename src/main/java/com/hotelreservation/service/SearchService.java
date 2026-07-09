package com.hotelreservation.service;

import com.hotelreservation.domain.Hotel;
import com.hotelreservation.dto.PageResponse;
import com.hotelreservation.dto.hotel.HotelResponse;
import com.hotelreservation.dto.search.HotelSearchRequest;
import com.hotelreservation.mapper.HotelMapper;
import com.hotelreservation.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.hotelreservation.service.HotelSpecifications.activeOnly;
import static com.hotelreservation.service.HotelSpecifications.cityEquals;
import static com.hotelreservation.service.HotelSpecifications.minStars;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    /**
     * Paginated, sorted, filtered search. Specifications are AND-combined and
     * null-safe, so absent criteria simply drop out of the query.
     */
    @Transactional(readOnly = true)
    public PageResponse<HotelResponse> search(HotelSearchRequest req, Pageable pageable) {
        Specification<Hotel> spec = activeOnly()
                .and(cityEquals(req.city()))
                .and(minStars(req.starRating()));
        Page<HotelResponse> page = hotelRepository.findAll(spec, pageable).map(hotelMapper::toResponse);
        return PageResponse.from(page);
    }
}
