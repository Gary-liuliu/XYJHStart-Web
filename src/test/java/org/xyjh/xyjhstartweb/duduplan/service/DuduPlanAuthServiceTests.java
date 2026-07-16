package org.xyjh.xyjhstartweb.duduplan.service;

import org.junit.jupiter.api.Test;
import org.xyjh.xyjhstartweb.duduplan.config.DuduPlanProperties;
import org.xyjh.xyjhstartweb.duduplan.dto.AuthResponse;
import org.xyjh.xyjhstartweb.duduplan.error.DuduPlanApiException;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanTokenService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuduPlanAuthServiceTests {
    @Test
    void fixedAccountsMapToExpectedRoles() {
        DuduPlanProperties properties = new DuduPlanProperties();
        properties.setOwnerPassword("owner-password");
        properties.setObserverPassword("observer-password");
        properties.setTokenSecret("test-secret-with-at-least-thirty-two-bytes");
        DuduPlanAuthService authService = new DuduPlanAuthService(properties, new DuduPlanTokenService(properties));

        AuthResponse owner = authService.login("嘟嘟", "owner-password");
        AuthResponse observer = authService.login("肚肚", "observer-password");

        assertThat(owner.role()).isEqualTo("owner");
        assertThat(observer.role()).isEqualTo("observer");
    }

    @Test
    void invalidAccountAndPasswordUseSameError() {
        DuduPlanProperties properties = new DuduPlanProperties();
        properties.setOwnerPassword("owner-password");
        DuduPlanAuthService authService = new DuduPlanAuthService(properties, new DuduPlanTokenService(properties));

        assertThatThrownBy(() -> authService.login("不存在", "owner-password"))
                .isInstanceOfSatisfying(DuduPlanApiException.class,
                        exception -> assertThat(exception.getError()).isEqualTo("invalid_credentials"));
        assertThatThrownBy(() -> authService.login("嘟嘟", "wrong-password"))
                .isInstanceOfSatisfying(DuduPlanApiException.class,
                        exception -> assertThat(exception.getError()).isEqualTo("invalid_credentials"));
    }
}
