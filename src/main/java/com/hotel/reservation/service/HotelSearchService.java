package com.hotel.reservation.service;

import com.hotel.reservation.domain.Hotel;
import com.hotel.reservation.dto.hotel.HotelResponse;
import com.hotel.reservation.dto.search.HotelSearchCriteria;
import com.hotel.reservation.mapper.HotelMapper;
import com.hotel.reservation.repository.HotelRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic search using JPA Specifications. Each non-null criterion becomes a
 * predicate; results are paginated and sorted by the incoming Pageable.
 */
@Service
@RequiredArgsConstructor
public class HotelSearchService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    @Transactional(readOnly = true)
    public Page<HotelResponse> search(HotelSearchCriteria c, Pageable pageable) {
        Specification<Hotel> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (c.city() != null && !c.city().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("city")), c.city().toLowerCase()));
            }
            if (c.starRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("starRating"), c.starRating()));
            }
            if (c.amenities() != null && !c.amenities().isEmpty()) {
                predicates.add(root.join("amenities").in(c.amenities()));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return hotelRepository.findAll(spec, pageable).map(hotelMapper::toResponse);
    }
}
