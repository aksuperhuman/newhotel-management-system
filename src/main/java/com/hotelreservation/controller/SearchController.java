package com.hotelreservation.controller;

import com.hotelreservation.dto.PageResponse;
import com.hotelreservation.dto.hotel.HotelResponse;
import com.hotelreservation.dto.search.HotelSearchRequest;
import com.hotelreservation.service.SearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search")
@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/search")
    public ResponseEntity<PageResponse<HotelResponse>> search(
            @RequestBody HotelSearchRequest req,
            @PageableDefault(size = 20, sort = "starRating") Pageable pageable) {
        return ResponseEntity.ok(searchService.search(req, pageable));
    }
}
