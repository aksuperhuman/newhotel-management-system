package com.hotelreservation.service;

import com.hotelreservation.domain.Hotel;
import org.springframework.data.jpa.domain.Specification;

/** Composable JPA Specifications backing the search engine's dynamic filters. */
public final class HotelSpecifications {

    private HotelSpecifications() {}

    public static Specification<Hotel> activeOnly() {
        return (root, q, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Hotel> cityEquals(String city) {
        return (root, q, cb) -> city == null ? null
                : cb.equal(cb.lower(root.get("city")), city.toLowerCase());
    }

    public static Specification<Hotel> minStars(Integer stars) {
        return (root, q, cb) -> stars == null ? null
                : cb.greaterThanOrEqualTo(root.get("starRating"), stars);
    }
}
