package com.hotel.reservation.controller;

import com.hotel.reservation.dto.hotel.HotelRequest;
import com.hotel.reservation.dto.hotel.HotelResponse;
import com.hotel.reservation.dto.room.RoomRequest;
import com.hotel.reservation.dto.room.RoomResponse;
import com.hotel.reservation.dto.search.HotelSearchCriteria;
import com.hotel.reservation.security.CurrentUserService;
import com.hotel.reservation.service.HotelSearchService;
import com.hotel.reservation.service.HotelService;
import com.hotel.reservation.service.RoomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Hotels & Rooms")
@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final HotelSearchService searchService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HOTEL_MANAGER','ADMIN')")
    public ResponseEntity<HotelResponse> create(@Valid @RequestBody HotelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(hotelService.create(request, currentUserService.currentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HOTEL_MANAGER','ADMIN')")
    public ResponseEntity<HotelResponse> update(@PathVariable Long id, @Valid @RequestBody HotelRequest request) {
        return ResponseEntity.ok(hotelService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HOTEL_MANAGER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.get(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<HotelResponse>> search(HotelSearchCriteria criteria, Pageable pageable) {
        return ResponseEntity.ok(searchService.search(criteria, pageable));
    }

    @PostMapping("/{hotelId}/rooms")
    @PreAuthorize("hasAnyRole('HOTEL_MANAGER','ADMIN')")
    public ResponseEntity<RoomResponse> addRoom(@PathVariable Long hotelId, @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.addRoom(hotelId, request));
    }

    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<RoomResponse>> rooms(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.listByHotel(hotelId));
    }
}
