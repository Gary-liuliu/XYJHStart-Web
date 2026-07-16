package org.xyjh.xyjhstartweb.duduplan.realtime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.xyjh.xyjhstartweb.duduplan.dto.ChatMessageRequest;
import org.xyjh.xyjhstartweb.duduplan.dto.ChatMessageResponse;
import org.xyjh.xyjhstartweb.duduplan.error.DuduPlanApiException;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanRole;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanTokenService;
import org.xyjh.xyjhstartweb.duduplan.service.DuduPlanChatService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DuduPlanWebSocketHandler extends AbstractWebSocketHandler {
    private static final int MAX_MESSAGE_BYTES = 65_536;
    private static final long AUTHENTICATION_TIMEOUT_MILLIS = 5_000L;
    private static final long HEARTBEAT_TIMEOUT_MILLIS = 75_000L;
    private static final CloseStatus AUTHENTICATION_TIMEOUT = new CloseStatus(4002, "authentication_timeout");
    private static final CloseStatus AUTHENTICATION_FAILED = new CloseStatus(4003, "authentication_failed");

    private final ObjectMapper objectMapper;
    private final DuduPlanTokenService tokenService;
    private final DuduPlanChatService chatService;
    private final DuduPlanSessionRegistry sessionRegistry;
    private final ConcurrentHashMap<String, ConnectionState> connections = new ConcurrentHashMap<>();

    public DuduPlanWebSocketHandler(ObjectMapper objectMapper, DuduPlanTokenService tokenService,
                                    DuduPlanChatService chatService, DuduPlanSessionRegistry sessionRegistry) {
        this.objectMapper = objectMapper;
        this.tokenService = tokenService;
        this.chatService = chatService;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        long now = System.currentTimeMillis();
        connections.put(session.getId(), new ConnectionState(session, now + AUTHENTICATION_TIMEOUT_MILLIS,
                now, null, 0));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (message.getPayload().getBytes(StandardCharsets.UTF_8).length > MAX_MESSAGE_BYTES) {
            session.close(CloseStatus.TOO_BIG_TO_PROCESS);
            return;
        }
        ConnectionState state = connections.get(session.getId());
        if (state == null) {
            session.close(CloseStatus.SERVER_ERROR);
            return;
        }
        state.lastSeenAt = System.currentTimeMillis();

        JsonNode payload;
        try {
            payload = objectMapper.readTree(message.getPayload());
        } catch (Exception exception) {
            sendError(state, "invalid_json");
            return;
        }
        if (payload.path("protocolVersion").asInt(-1) != 2) {
            sendError(state, "unsupported_protocol_version");
            return;
        }
        String type = payload.path("type").asText("");
        if (state.role == null) {
            if (!"authenticate".equals(type)) {
                closeQuietly(state.session, AUTHENTICATION_FAILED);
                return;
            }
            authenticate(state, type, payload);
            return;
        }
        if (System.currentTimeMillis() >= state.accessTokenExpiresAt) {
            closeQuietly(session, AUTHENTICATION_FAILED);
            return;
        }
        handleAuthenticatedMessage(state, type, payload);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        session.close(CloseStatus.NOT_ACCEPTABLE.withReason("text_json_only"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        ConnectionState removed = connections.remove(session.getId());
        if (removed != null && removed.role != null) {
            sessionRegistry.removeIfCurrent(removed.role, session.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        closeQuietly(session, CloseStatus.SERVER_ERROR);
    }

    @Scheduled(fixedRate = 1_000L)
    public void closeExpiredConnections() {
        long now = System.currentTimeMillis();
        for (ConnectionState state : connections.values()) {
            if (state.role == null && now >= state.authenticationDeadline) {
                closeQuietly(state.session, AUTHENTICATION_TIMEOUT);
            } else if (state.role != null && (now >= state.accessTokenExpiresAt
                    || now - state.lastSeenAt >= HEARTBEAT_TIMEOUT_MILLIS)) {
                closeQuietly(state.session, AUTHENTICATION_FAILED);
            }
        }
    }

    private void authenticate(ConnectionState state, String type, JsonNode payload) {
        if (!"authenticate".equals(type) || !payload.path("accessToken").isTextual()) {
            closeQuietly(state.session, AUTHENTICATION_FAILED);
            return;
        }
        try {
            DuduPlanTokenService.TokenClaims claims = tokenService.parseAccessToken(payload.path("accessToken").asText());
            state.role = claims.role();
            state.accessTokenExpiresAt = claims.expiresAt();
            state.session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "protocolVersion", 2, "type", "authenticated", "role", state.role.value(),
                    "serverTime", System.currentTimeMillis(), "accessTokenExpiresAt", state.accessTokenExpiresAt
            ))));
            sessionRegistry.register(state.role, state.session);
        } catch (Exception exception) {
            closeQuietly(state.session, AUTHENTICATION_FAILED);
        }
    }

    private void handleAuthenticatedMessage(ConnectionState state, String type, JsonNode payload) {
        switch (type) {
            case "heartbeat" -> sessionRegistry.sendToSession(state.session, Map.of(
                    "protocolVersion", 2, "type", "heartbeat", "sentAt", System.currentTimeMillis()
            ));
            case "chat_message" -> handleChatMessage(state, payload);
            case "snapshot", "session_upsert", "workout_event" -> forwardOwnerTrainingEvent(state, type, payload);
            case "event_ack" -> forwardObserverAcknowledgement(state, payload);
            default -> sendError(state, "unsupported_message_type");
        }
    }

    private void handleChatMessage(ConnectionState state, JsonNode payload) {
        String messageId = payload.path("messageId").isTextual() ? payload.path("messageId").asText() : null;
        try {
            ChatMessageRequest request = objectMapper.treeToValue(payload, ChatMessageRequest.class);
            ChatMessageResponse saved = chatService.saveMessage(state.role, request);
            sessionRegistry.sendToSession(state.session, Map.of(
                    "protocolVersion", 2, "type", "chat_saved", "message", saved
            ));
            sessionRegistry.sendToRole(state.role.peer(), Map.of(
                    "protocolVersion", 2, "type", "chat_message", "message", saved
            ));
        } catch (DuduPlanApiException exception) {
            sendError(state, exception.getError(), messageId);
        } catch (Exception exception) {
            sendError(state, "invalid_chat_message", messageId);
        }
    }

    private void forwardOwnerTrainingEvent(ConnectionState state, String type, JsonNode payload) {
        if (state.role != DuduPlanRole.OWNER) {
            sendError(state, "owner_only_message");
            return;
        }
        if (!hasRequiredTrainingStructure(type, payload)) {
            sendError(state, "invalid_training_message");
            return;
        }
        sessionRegistry.sendToRole(DuduPlanRole.OBSERVER, asEventMap(payload));
    }

    private void forwardObserverAcknowledgement(ConnectionState state, JsonNode payload) {
        if (state.role != DuduPlanRole.OBSERVER || !payload.path("eventId").isTextual()) {
            sendError(state, "invalid_event_ack");
            return;
        }
        sessionRegistry.sendToRole(DuduPlanRole.OWNER, asEventMap(payload));
    }

    private boolean hasRequiredTrainingStructure(String type, JsonNode payload) {
        return switch (type) {
            case "snapshot" -> payload.path("snapshotId").isTextual()
                    && payload.path("chunkIndex").canConvertToInt()
                    && payload.path("chunkCount").canConvertToInt()
                    && payload.path("chunkIndex").asInt() >= 0
                    && payload.path("chunkCount").asInt() > 0
                    && payload.path("chunkIndex").asInt() < payload.path("chunkCount").asInt()
                    && payload.path("sessions").isArray();
            case "session_upsert" -> payload.has("session")
                    && (payload.path("session").path("sessionId").isTextual()
                    || payload.path("session").path("id").isTextual())
                    && (payload.path("session").path("updatedAt").isNumber()
                    || payload.path("session").path("updatedAt").isTextual());
            case "workout_event" -> payload.path("eventId").isTextual() && payload.path("sessionId").isTextual()
                    && payload.path("eventType").isTextual();
            default -> false;
        };
    }

    private Map<String, Object> asEventMap(JsonNode payload) {
        return objectMapper.convertValue(payload, new TypeReference<>() {});
    }

    private void sendError(ConnectionState state, String error) {
        sendError(state, error, null);
    }

    private void sendError(ConnectionState state, String error, String messageId) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("protocolVersion", 2);
        event.put("type", "error");
        event.put("error", error);
        event.put("serverTime", System.currentTimeMillis());
        if (messageId != null && !messageId.isBlank()) {
            event.put("messageId", messageId);
        }
        if (state.role != null) {
            sessionRegistry.sendToSession(state.session, event);
            return;
        }
        try {
            state.session.sendMessage(new TextMessage(objectMapper.writeValueAsString(event)));
        } catch (IOException exception) {
            closeQuietly(state.session, CloseStatus.SERVER_ERROR);
        }
    }

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException ignored) {
        }
    }

    private static final class ConnectionState {
        private final WebSocketSession session;
        private final long authenticationDeadline;
        private volatile long lastSeenAt;
        private volatile DuduPlanRole role;
        private volatile long accessTokenExpiresAt;

        private ConnectionState(WebSocketSession session, long authenticationDeadline, long lastSeenAt,
                                DuduPlanRole role, long accessTokenExpiresAt) {
            this.session = session;
            this.authenticationDeadline = authenticationDeadline;
            this.lastSeenAt = lastSeenAt;
            this.role = role;
            this.accessTokenExpiresAt = accessTokenExpiresAt;
        }
    }
}
