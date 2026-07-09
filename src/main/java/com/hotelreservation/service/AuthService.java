package com.hotelreservation.service;

import com.hotelreservation.domain.RefreshToken;
import com.hotelreservation.domain.User;
import com.hotelreservation.dto.auth.AuthResponse;
import com.hotelreservation.dto.auth.LoginRequest;
import com.hotelreservation.dto.auth.RefreshRequest;
import com.hotelreservation.dto.auth.RegisterRequest;
import com.hotelreservation.exception.BusinessException;
import com.hotelreservation.repository.RefreshTokenRepository;
import com.hotelreservation.repository.UserRepository;
import com.hotelreservation.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessExpirationMs;
    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email already registered", HttpStatus.CONFLICT);
        }
        User user = User.builder()
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .role(req.role())
                .enabled(true)
                .build();
        userRepository.save(user);
        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken stored = refreshTokenRepository.findByToken(req.refreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token expired or revoked", HttpStatus.UNAUTHORIZED);
        }
        return issueTokens(stored.getUser());
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = UUID.randomUUID().toString() + UUID.randomUUID();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(Instant.now().plus(refreshExpirationMs, ChronoUnit.MILLIS))
                .revoked(false)
                .build());
        return new AuthResponse(accessToken, refreshToken, "Bearer", accessExpirationMs);
    }
}
