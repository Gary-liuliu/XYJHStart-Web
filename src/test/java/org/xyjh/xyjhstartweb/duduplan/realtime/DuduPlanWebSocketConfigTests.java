package org.xyjh.xyjhstartweb.duduplan.realtime;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

class DuduPlanWebSocketConfigTests {
    @Test
    void configuresWebSocketMessageBuffersForProtocolPayloads() {
        DuduPlanWebSocketConfig config = new DuduPlanWebSocketConfig(null);

        ServletServerContainerFactoryBean container = config.webSocketContainer();

        assertThat(container.getMaxTextMessageBufferSize()).isEqualTo(128 * 1024);
        assertThat(container.getMaxBinaryMessageBufferSize()).isEqualTo(128 * 1024);
    }
}
