package com.hotelreservation.controller;

import com.hotelreservation.dto.hotel.HotelRequest;
import com.hotelreservation.dto.hotel.HotelResponse;
import com.hotelreservation.security.SecurityUtils;
import com.hotelreservation.service.HotelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Hotels")
@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasAnyRole('HOTEL_MANAGER','ADMIN')")
    public ResponseEntity<HotelResponse> create(@Valid @RequestBody HotelRequest req) {
        return ResponseEntity.ok(hotelService.create(req, securityUtils.currentEmail()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HOTEL_MANAGER','ADMIN')")
    public ResponseEntity<HotelResponse> update(@PathVariable Long id, @Valid @RequestBody HotelRequest req) {
        return ResponseEntity.ok(hotelService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HOTEL_MANAGER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('HOTEL_MANAGER','ADMIN')")
    public ResponseEntity<Void> addImage(@PathVariable Long id, @RequestParam String url) {
        hotelService.addImage(id, url);
        return ResponseEntity.accepted().build();
    }
}
