package org.xyjh.xyjhstartweb.duduplan.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.xyjh.xyjhstartweb.duduplan.config.DuduPlanProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class DuduPlanTokenService {
    public static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(15);
    public static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(365);

    private final DuduPlanProperties properties;
    private final Clock clock = Clock.systemUTC();

    public DuduPlanTokenService(DuduPlanProperties properties) {
        this.properties = properties;
    }

    public IssuedTokens issueTokens(DuduPlanRole role) {
        Instant issuedAt = clock.instant();
        Instant accessExpiresAt = issuedAt.plus(ACCESS_TOKEN_LIFETIME);
        Instant refreshExpiresAt = issuedAt.plus(REFRESH_TOKEN_LIFETIME);
        return new IssuedTokens(
                createToken(role, "access", issuedAt, accessExpiresAt), accessExpiresAt.toEpochMilli(),
                createToken(role, "refresh", issuedAt, refreshExpiresAt), refreshExpiresAt.toEpochMilli()
        );
    }

    public TokenClaims parseAccessToken(String token) {
        return parseToken(token, "access");
    }

    public TokenClaims parseRefreshToken(String token) {
        return parseToken(token, "refresh");
    }

    private String createToken(DuduPlanRole role, String purpose, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .setSubject(role.accountName())
                .claim("role", role.value())
                .claim("purpose", purpose)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    private TokenClaims parseToken(String token, String requiredPurpose) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        String purpose = claims.get("purpose", String.class);
        if (!requiredPurpose.equals(purpose)) {
            throw new IllegalArgumentException("Token purpose mismatch");
        }
        DuduPlanRole role = DuduPlanRole.fromValue(claims.get("role", String.class));
        return new TokenClaims(role, claims.getExpiration().getTime());
    }

    private SecretKey signingKey() {
        String tokenSecret = properties.getTokenSecret();
        if (tokenSecret == null || tokenSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("dudu-plan.token-secret must contain at least 32 UTF-8 bytes");
        }
        return Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));
    }

    public record IssuedTokens(String accessToken, long accessTokenExpiresAt,
                               String refreshToken, long refreshTokenExpiresAt) {
    }

    public record TokenClaims(DuduPlanRole role, long expiresAt) {
    }
}
