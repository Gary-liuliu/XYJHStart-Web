package org.xyjh.xyjhstartweb.duduplan.security;

import org.junit.jupiter.api.Test;
import org.xyjh.xyjhstartweb.duduplan.config.DuduPlanProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuduPlanTokenServiceTests {
    @Test
    void tokenServiceHasOneProductionConstructor() {
        assertThat(DuduPlanTokenService.class.getDeclaredConstructors()).hasSize(1);
    }

    @Test
    void accessAndRefreshTokensHaveSeparatePurposes() {
        DuduPlanProperties properties = new DuduPlanProperties();
        properties.setTokenSecret("test-secret-with-at-least-thirty-two-bytes");
        DuduPlanTokenService tokenService = new DuduPlanTokenService(properties);

        DuduPlanTokenService.IssuedTokens tokens = tokenService.issueTokens(DuduPlanRole.OWNER);

        assertThat(tokenService.parseAccessToken(tokens.accessToken()).role()).isEqualTo(DuduPlanRole.OWNER);
        assertThat(tokenService.parseRefreshToken(tokens.refreshToken()).role()).isEqualTo(DuduPlanRole.OWNER);
        assertThatThrownBy(() -> tokenService.parseAccessToken(tokens.refreshToken()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tokenSecretMustBeAtLeastThirtyTwoBytes() {
        DuduPlanProperties properties = new DuduPlanProperties();
        properties.setTokenSecret("too-short");
        DuduPlanTokenService tokenService = new DuduPlanTokenService(properties);

        assertThatThrownBy(() -> tokenService.issueTokens(DuduPlanRole.OBSERVER))
                .isInstanceOf(IllegalStateException.class);
    }
}
