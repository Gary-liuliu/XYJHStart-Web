package org.xyjh.xyjhstartweb.duduplan.dto;

public record AuthResponse(String accountName, String role, String accessToken, long accessTokenExpiresAt,
                           String refreshToken, long refreshTokenExpiresAt) {
}
