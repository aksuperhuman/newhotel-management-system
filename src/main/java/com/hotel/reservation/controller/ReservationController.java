package com.hotel.reservation.controller;

import com.hotel.reservation.dto.reservation.ReservationRequest;
import com.hotel.reservation.dto.reservation.ReservationResponse;
import com.hotel.reservation.security.CurrentUserService;
import com.hotel.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservations")
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final CurrentUserService currentUserService;

    @Operation(summary = "Create a booking (high-concurrency engine)")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReservationResponse> book(@Valid @RequestBody ReservationRequest request) {
        Long userId = currentUserService.currentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.book(userId, request));
    }

    @Operation(summary = "Cancel a reservation and release inventory")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancel(id));
    }
}
