package com.hotel.reservation.service;

import com.hotel.reservation.domain.RefreshToken;
import com.hotel.reservation.domain.User;
import com.hotel.reservation.dto.auth.*;
import com.hotel.reservation.exception.BusinessException;
import com.hotel.reservation.repository.RefreshTokenRepository;
import com.hotel.reservation.repository.UserRepository;
import com.hotel.reservation.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already registered", HttpStatus.CONFLICT);
        }
        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .fullName(request.fullName())
            .role(request.role())
            .enabled(true)
            .build();
        userRepository.save(user);
        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException("Refresh token expired", HttpStatus.UNAUTHORIZED);
        }
        User user = userRepository.findById(stored.getUserId()).orElseThrow();
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String access = jwtService.generateAccessToken(userDetails);
        RefreshToken refresh = RefreshToken.builder()
            .userId(user.getId())
            .token(UUID.randomUUID().toString())
            .expiresAt(OffsetDateTime.now().plusSeconds(refreshExpirationMs / 1000))
            .revoked(false)
            .build();
        refreshTokenRepository.save(refresh);
        return AuthResponse.of(access, refresh.getToken());
    }
}
