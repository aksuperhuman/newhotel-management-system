package com.hotelreservation.security;

import com.hotelreservation.domain.User;
import com.hotelreservation.exception.BusinessException;
import com.hotelreservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Resolves the currently authenticated user's id from the security context. */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public Long currentUserId() {
        String email = currentEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Not authenticated", HttpStatus.UNAUTHORIZED));
        return user.getId();
    }

    public String currentEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("Not authenticated", HttpStatus.UNAUTHORIZED);
        }
        return auth.getName();
    }
}
