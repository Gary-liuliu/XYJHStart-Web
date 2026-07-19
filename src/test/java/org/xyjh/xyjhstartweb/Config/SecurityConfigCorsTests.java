package org.xyjh.xyjhstartweb.Config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigCorsTests {
    @Test
    void allowsProductionAndroidWebSocketOrigin() {
        SecurityConfig securityConfig = new SecurityConfig(null, null);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/dudu-plan/realtime");
        CorsConfiguration corsConfiguration = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(request);

        assertThat(corsConfiguration).isNotNull();
        assertThat(corsConfiguration.checkOrigin("https://kakaweb.ltd"))
                .isEqualTo("https://kakaweb.ltd");
    }
}
