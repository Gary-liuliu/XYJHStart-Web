package org.xyjh.xyjhstartweb.duduplan.realtime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class DuduPlanWebSocketConfig implements WebSocketConfigurer {
    private static final int WEBSOCKET_MESSAGE_BUFFER_BYTES = 128 * 1024;

    private final DuduPlanWebSocketHandler webSocketHandler;

    public DuduPlanWebSocketConfig(DuduPlanWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Bean
    public ServletServerContainerFactoryBean webSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(WEBSOCKET_MESSAGE_BUFFER_BYTES);
        container.setMaxBinaryMessageBufferSize(WEBSOCKET_MESSAGE_BUFFER_BYTES);
        return container;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/api/dudu-plan/realtime")
                .setAllowedOrigins("*");
    }
}
