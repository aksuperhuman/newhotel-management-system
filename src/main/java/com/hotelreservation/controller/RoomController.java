package com.hotelreservation.controller;

import com.hotelreservation.dto.room.RoomRequest;
import com.hotelreservation.dto.room.RoomResponse;
import com.hotelreservation.service.RoomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Rooms")
@RestController
@RequestMapping("/api/v1/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HOTEL_MANAGER','ADMIN')")
    public ResponseEntity<RoomResponse> addRoom(@PathVariable Long hotelId, @Valid @RequestBody RoomRequest req) {
        return ResponseEntity.ok(roomService.addRoom(hotelId, req));
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> list(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.listByHotel(hotelId));
    }
}
