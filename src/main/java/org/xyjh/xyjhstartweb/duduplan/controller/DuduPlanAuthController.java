package org.xyjh.xyjhstartweb.duduplan.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xyjh.xyjhstartweb.duduplan.dto.AuthResponse;
import org.xyjh.xyjhstartweb.duduplan.dto.LoginRequest;
import org.xyjh.xyjhstartweb.duduplan.dto.RefreshRequest;
import org.xyjh.xyjhstartweb.duduplan.error.DuduPlanApiException;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanLoginRateLimiter;
import org.xyjh.xyjhstartweb.duduplan.service.DuduPlanAuthService;

@RestController
@RequestMapping("/api/dudu-plan/auth")
public class DuduPlanAuthController {
    private final DuduPlanAuthService authService;
    private final DuduPlanLoginRateLimiter rateLimiter;

    public DuduPlanAuthController(DuduPlanAuthService authService, DuduPlanLoginRateLimiter rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String rateLimitKey = clientAddress(servletRequest) + ":" + request.username();
        if (rateLimiter.isBlocked(rateLimitKey)) {
            throw new DuduPlanApiException(HttpStatus.TOO_MANY_REQUESTS, "too_many_attempts");
        }
        try {
            AuthResponse response = authService.login(request.username(), request.password());
            rateLimiter.recordSuccess(rateLimitKey);
            return response;
        } catch (DuduPlanApiException exception) {
            if (exception.getStatus() == HttpStatus.UNAUTHORIZED) {
                rateLimiter.recordFailure(rateLimitKey);
            }
            throw exception;
        }
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    private String clientAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }
}
