package com.hotelreservation.service;

import com.hotelreservation.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "2f5a8c1e9b7d4f6a0c3e5d7b9f1a3c5e7d9b1f3a5c7e9d1b3f5a7c9e1d3b5f7a", 900000);

    @Test
    void generatesAndValidatesToken() {
        String token = jwtService.generateAccessToken("user@hotel.com", "CUSTOMER");
        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("user@hotel.com");
    }

    @Test
    void rejectsGarbageToken() {
        assertThat(jwtService.isValid("not-a-real-token")).isFalse();
    }
}
