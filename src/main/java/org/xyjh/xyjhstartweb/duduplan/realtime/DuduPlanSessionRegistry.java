package org.xyjh.xyjhstartweb.duduplan.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanRole;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

@Component
public class DuduPlanSessionRegistry implements DuduPlanRealtimeGateway {
    private static final CloseStatus REPLACED = new CloseStatus(4001, "replaced_by_new_connection");

    private final ObjectMapper objectMapper;
    private final EnumMap<DuduPlanRole, RegisteredSession> sessions = new EnumMap<>(DuduPlanRole.class);

    public DuduPlanSessionRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(DuduPlanRole role, WebSocketSession rawSession) {
        RegisteredSession replacement = new RegisteredSession(rawSession,
                new ConcurrentWebSocketSessionDecorator(rawSession, 5_000, 65_536));
        RegisteredSession previous;
        synchronized (sessions) {
            previous = sessions.put(role, replacement);
        }
        if (previous != null && !previous.rawSession().getId().equals(rawSession.getId())) {
            closeQuietly(previous.rawSession(), REPLACED);
        }
        publishPresence(role == DuduPlanRole.OBSERVER || bothOnline());
    }

    public void removeIfCurrent(DuduPlanRole role, String sessionId) {
        boolean removed = false;
        synchronized (sessions) {
            RegisteredSession current = sessions.get(role);
            if (current != null && current.rawSession().getId().equals(sessionId)) {
                sessions.remove(role);
                removed = true;
            }
        }
        if (removed) {
            publishPresence(false);
        }
    }

    @Override
    public void sendToRole(DuduPlanRole role, Map<String, Object> event) {
        RegisteredSession registered;
        synchronized (sessions) {
            registered = sessions.get(role);
        }
        if (registered == null || !registered.rawSession().isOpen()) {
            return;
        }
        try {
            registered.concurrentSession().sendMessage(new TextMessage(objectMapper.writeValueAsString(event)));
        } catch (IOException exception) {
            closeQuietly(registered.rawSession(), CloseStatus.SERVER_ERROR);
        }
    }

    public void sendToSession(WebSocketSession session, Map<String, Object> event) {
        RegisteredSession registered = null;
        synchronized (sessions) {
            for (RegisteredSession candidate : sessions.values()) {
                if (candidate.rawSession().getId().equals(session.getId())) {
                    registered = candidate;
                    break;
                }
            }
        }
        try {
            String payload = objectMapper.writeValueAsString(event);
            if (registered != null) {
                registered.concurrentSession().sendMessage(new TextMessage(payload));
            } else if (session.isOpen()) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (IOException exception) {
            closeQuietly(session, CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public boolean isOnline(DuduPlanRole role) {
        synchronized (sessions) {
            RegisteredSession session = sessions.get(role);
            return session != null && session.rawSession().isOpen();
        }
    }

    private boolean bothOnline() {
        return isOnline(DuduPlanRole.OWNER) && isOnline(DuduPlanRole.OBSERVER);
    }

    private void publishPresence(boolean requestSnapshot) {
        boolean ownerOnline = isOnline(DuduPlanRole.OWNER);
        boolean observerOnline = isOnline(DuduPlanRole.OBSERVER);
        long serverTime = System.currentTimeMillis();
        Map<String, Object> observerEvent = Map.of(
                "protocolVersion", 2, "type", "presence", "ownerOnline", ownerOnline,
                "observerOnline", observerOnline, "serverTime", serverTime
        );
        sendToRole(DuduPlanRole.OBSERVER, observerEvent);
        sendToRole(DuduPlanRole.OWNER, Map.of(
                "protocolVersion", 2, "type", "presence", "ownerOnline", ownerOnline,
                "observerOnline", observerOnline, "requestSnapshot", requestSnapshot && ownerOnline && observerOnline,
                "serverTime", serverTime
        ));
    }

    private void closeQuietly(WebSocketSession session, CloseStatus closeStatus) {
        try {
            if (session.isOpen()) {
                session.close(closeStatus);
            }
        } catch (IOException ignored) {
        }
    }

    private record RegisteredSession(WebSocketSession rawSession,
                                     ConcurrentWebSocketSessionDecorator concurrentSession) {
    }
}
