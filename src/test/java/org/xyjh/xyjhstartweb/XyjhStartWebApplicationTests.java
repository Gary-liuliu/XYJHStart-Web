package org.xyjh.xyjhstartweb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class XyjhStartWebApplicationTests {
    @Test
    void applicationEntryPointRemainsConfigured() {
        assertThat(XyjhStartWebApplication.class).hasAnnotation(SpringBootApplication.class);
    }
}
