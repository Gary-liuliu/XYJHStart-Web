package org.xyjh.xyjhstartweb.duduplan.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DuduPlanAccessTokenFilterTests {
    @Test
    void downstreamExceptionsAreNotConvertedToUnauthorized() throws Exception {
        DuduPlanTokenService tokenService = mock(DuduPlanTokenService.class);
        when(tokenService.parseAccessToken("valid-token"))
                .thenReturn(new DuduPlanTokenService.TokenClaims(DuduPlanRole.OWNER, Long.MAX_VALUE));
        DuduPlanAccessTokenFilter filter = new DuduPlanAccessTokenFilter(tokenService, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/dudu-plan/chat/read");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
            throw new ServletException("downstream_failure");
        })).isInstanceOf(ServletException.class).hasMessage("downstream_failure");
    }
}
