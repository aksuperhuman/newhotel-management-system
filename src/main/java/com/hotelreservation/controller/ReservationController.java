package com.hotelreservation.controller;

import com.hotelreservation.dto.reservation.ReservationRequest;
import com.hotelreservation.dto.reservation.ReservationResponse;
import com.hotelreservation.security.SecurityUtils;
import com.hotelreservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Reservations")
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Book a room (concurrency-safe)")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReservationResponse> book(@Valid @RequestBody ReservationRequest req) {
        return ResponseEntity.ok(reservationService.book(req, securityUtils.currentUserId()));
    }

    @Operation(summary = "Cancel a reservation and release inventory")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancel(id, securityUtils.currentUserId()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ReservationResponse>> myReservations() {
        return ResponseEntity.ok(reservationService.myReservations(securityUtils.currentUserId()));
    }
}
