package org.xyjh.xyjhstartweb.duduplan.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class DuduPlanWebSocketConfig implements WebSocketConfigurer {
    private final DuduPlanWebSocketHandler webSocketHandler;

    public DuduPlanWebSocketConfig(DuduPlanWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/api/dudu-plan/realtime")
                .setAllowedOrigins("*");
    }
}
