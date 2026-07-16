package org.xyjh.xyjhstartweb.duduplan.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.xyjh.xyjhstartweb.duduplan.config.DuduPlanProperties;
import org.xyjh.xyjhstartweb.duduplan.dto.AuthResponse;
import org.xyjh.xyjhstartweb.duduplan.error.DuduPlanApiException;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanRole;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanTokenService;

@Service
public class DuduPlanAuthService {
    private final DuduPlanProperties properties;
    private final PasswordEncoder passwordEncoder;
    private final DuduPlanTokenService tokenService;

    public DuduPlanAuthService(DuduPlanProperties properties, PasswordEncoder passwordEncoder,
                               DuduPlanTokenService tokenService) {
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public AuthResponse login(String username, String password) {
        DuduPlanRole role = DuduPlanRole.fromAccountName(username);
        String passwordHash = role == null ? null : passwordHashFor(role);
        if (role == null || passwordHash == null || passwordHash.isBlank()
                || !passwordEncoder.matches(password, passwordHash)) {
            throw new DuduPlanApiException(HttpStatus.UNAUTHORIZED, "invalid_credentials");
        }
        return createResponse(role);
    }

    public AuthResponse refresh(String refreshToken) {
        try {
            return createResponse(tokenService.parseRefreshToken(refreshToken).role());
        } catch (Exception exception) {
            throw new DuduPlanApiException(HttpStatus.UNAUTHORIZED, "invalid_refresh_token");
        }
    }

    private AuthResponse createResponse(DuduPlanRole role) {
        DuduPlanTokenService.IssuedTokens tokens;
        try {
            tokens = tokenService.issueTokens(role);
        } catch (IllegalStateException exception) {
            throw new DuduPlanApiException(HttpStatus.SERVICE_UNAVAILABLE, "service_not_configured");
        }
        return new AuthResponse(role.accountName(), role.value(), tokens.accessToken(), tokens.accessTokenExpiresAt(),
                tokens.refreshToken(), tokens.refreshTokenExpiresAt());
    }

    private String passwordHashFor(DuduPlanRole role) {
        return role == DuduPlanRole.OWNER ? properties.getOwnerPasswordHash() : properties.getObserverPasswordHash();
    }
}
