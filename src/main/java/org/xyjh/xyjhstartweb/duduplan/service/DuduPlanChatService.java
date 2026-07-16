package org.xyjh.xyjhstartweb.duduplan.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xyjh.xyjhstartweb.duduplan.dto.ChatMessageRequest;
import org.xyjh.xyjhstartweb.duduplan.dto.ChatMessageResponse;
import org.xyjh.xyjhstartweb.duduplan.entity.DuduChatMessage;
import org.xyjh.xyjhstartweb.duduplan.error.DuduPlanApiException;
import org.xyjh.xyjhstartweb.duduplan.mapper.DuduChatMessageMapper;
import org.xyjh.xyjhstartweb.duduplan.realtime.DuduPlanRealtimeGateway;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanRole;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class DuduPlanChatService {
    private static final int MAX_CONTENT_CODE_POINTS = 2_000;
    private static final long RECALL_WINDOW_MILLIS = 120_000L;

    private final DuduChatMessageMapper messageMapper;
    private final DuduPlanRealtimeGateway realtimeGateway;
    private final DuduPlanEncouragementRateLimiter encouragementRateLimiter;

    public DuduPlanChatService(DuduChatMessageMapper messageMapper, DuduPlanRealtimeGateway realtimeGateway,
                               DuduPlanEncouragementRateLimiter encouragementRateLimiter) {
        this.messageMapper = messageMapper;
        this.realtimeGateway = realtimeGateway;
        this.encouragementRateLimiter = encouragementRateLimiter;
    }

    @Transactional
    public ChatMessageResponse saveMessage(DuduPlanRole senderRole, ChatMessageRequest request) {
        ValidatedMessage validated = validateMessage(senderRole, request);
        DuduChatMessage existing = messageMapper.findByMessageId(validated.messageId());
        if (existing != null) {
            ensureIdempotentMatch(existing, senderRole, validated);
            return ChatMessageResponse.from(existing);
        }

        long now = System.currentTimeMillis();
        if ("encouragement".equals(validated.messageType())
                && !encouragementRateLimiter.tryAcquire(senderRole, now)) {
            throw new DuduPlanApiException(HttpStatus.TOO_MANY_REQUESTS, "encouragement_rate_limited");
        }

        DuduChatMessage message = new DuduChatMessage();
        message.setMessageId(validated.messageId());
        message.setSenderRole(senderRole.value());
        message.setReceiverRole(senderRole.peer().value());
        message.setMessageType(validated.messageType());
        message.setContent(validated.content());
        message.setReplyToMessageId(validated.replyToMessageId());
        message.setClientCreatedAt(validated.clientCreatedAt());
        message.setServerCreatedAt(now);
        try {
            messageMapper.insert(message);
        } catch (DuplicateKeyException exception) {
            DuduChatMessage concurrentlySaved = messageMapper.findByMessageId(validated.messageId());
            ensureIdempotentMatch(concurrentlySaved, senderRole, validated);
            return ChatMessageResponse.from(concurrentlySaved);
        }
        return ChatMessageResponse.from(message);
    }

    public List<ChatMessageResponse> history(DuduPlanRole role, Long beforeId, int limit) {
        if (beforeId != null && beforeId <= 0) {
            throw new DuduPlanApiException(HttpStatus.BAD_REQUEST, "invalid_before_id");
        }
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 50);
        return messageMapper.findHistory(role.value(), beforeId, safeLimit).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    public long unreadCount(DuduPlanRole role) {
        return messageMapper.countUnread(role.value());
    }

    @Transactional
    public List<ChatMessageResponse> markDelivered(DuduPlanRole receiverRole, List<String> messageIds) {
        List<String> uniqueIds = validateMessageIds(messageIds);
        List<DuduChatMessage> messages = messageMapper.findByMessageIds(uniqueIds);
        if (messages.size() != uniqueIds.size() || messages.stream()
                .anyMatch(message -> !receiverRole.value().equals(message.getReceiverRole()))) {
            throw new DuduPlanApiException(HttpStatus.FORBIDDEN, "not_message_receiver");
        }
        long deliveredAt = System.currentTimeMillis();
        messageMapper.markDelivered(receiverRole.value(), uniqueIds, deliveredAt);
        List<ChatMessageResponse> updated = messageMapper.findByMessageIds(uniqueIds).stream()
                .map(ChatMessageResponse::from).toList();
        realtimeGateway.sendToRole(receiverRole.peer(), Map.of(
                "protocolVersion", 2, "type", "chat_delivered", "messageIds", uniqueIds,
                "deliveredAt", deliveredAt
        ));
        return updated;
    }

    @Transactional
    public Map<String, Object> markRead(DuduPlanRole receiverRole, String upToMessageId) {
        DuduChatMessage boundary = messageMapper.findByMessageId(upToMessageId);
        if (boundary == null) {
            throw new DuduPlanApiException(HttpStatus.NOT_FOUND, "message_not_found");
        }
        if (!receiverRole.value().equals(boundary.getReceiverRole())) {
            throw new DuduPlanApiException(HttpStatus.FORBIDDEN, "not_message_receiver");
        }
        long readAt = System.currentTimeMillis();
        int updatedCount = messageMapper.markRead(receiverRole.value(), boundary.getId(), readAt);
        Map<String, Object> event = Map.of(
                "protocolVersion", 2, "type", "chat_read", "upToMessageId", upToMessageId,
                "readAt", readAt, "updatedCount", updatedCount
        );
        realtimeGateway.sendToRole(receiverRole.peer(), event);
        return event;
    }

    @Transactional
    public ChatMessageResponse recall(DuduPlanRole senderRole, String messageId) {
        DuduChatMessage message = messageMapper.findByMessageId(messageId);
        if (message == null) {
            throw new DuduPlanApiException(HttpStatus.NOT_FOUND, "message_not_found");
        }
        if (!senderRole.value().equals(message.getSenderRole())) {
            throw new DuduPlanApiException(HttpStatus.FORBIDDEN, "not_message_sender");
        }
        if (message.getRecalledAt() == null) {
            long recalledAt = System.currentTimeMillis();
            int updated = messageMapper.recall(messageId, senderRole.value(), recalledAt,
                    recalledAt - RECALL_WINDOW_MILLIS);
            if (updated == 0) {
                throw new DuduPlanApiException(HttpStatus.CONFLICT, "recall_window_expired");
            }
            message = messageMapper.findByMessageId(messageId);
            ChatMessageResponse response = ChatMessageResponse.from(message);
            realtimeGateway.sendToRole(senderRole.peer(), Map.of(
                    "protocolVersion", 2, "type", "chat_recalled", "message", response
            ));
            return response;
        }
        return ChatMessageResponse.from(message);
    }

    private ValidatedMessage validateMessage(DuduPlanRole senderRole, ChatMessageRequest request) {
        if (request == null || request.messageId() == null || request.messageType() == null
                || request.content() == null || request.clientCreatedAt() == null || request.clientCreatedAt() < 0) {
            throw new DuduPlanApiException(HttpStatus.BAD_REQUEST, "invalid_message");
        }
        try {
            UUID.fromString(request.messageId());
            if (request.replyToMessageId() != null) {
                UUID.fromString(request.replyToMessageId());
            }
        } catch (IllegalArgumentException exception) {
            throw new DuduPlanApiException(HttpStatus.BAD_REQUEST, "invalid_message_id");
        }
        String messageType = request.messageType().trim();
        if (!Set.of("text", "encouragement").contains(messageType)) {
            throw new DuduPlanApiException(HttpStatus.BAD_REQUEST, "invalid_message_type");
        }
        if ("encouragement".equals(messageType) && senderRole != DuduPlanRole.OBSERVER) {
            throw new DuduPlanApiException(HttpStatus.FORBIDDEN, "encouragement_observer_only");
        }
        String content = request.content().strip();
        if (content.isEmpty() || content.codePointCount(0, content.length()) > MAX_CONTENT_CODE_POINTS) {
            throw new DuduPlanApiException(HttpStatus.BAD_REQUEST, "invalid_content");
        }
        if (request.replyToMessageId() != null && messageMapper.findByMessageId(request.replyToMessageId()) == null) {
            throw new DuduPlanApiException(HttpStatus.BAD_REQUEST, "reply_message_not_found");
        }
        return new ValidatedMessage(request.messageId(), messageType, content, request.replyToMessageId(),
                request.clientCreatedAt());
    }

    private void ensureIdempotentMatch(DuduChatMessage existing, DuduPlanRole senderRole,
                                       ValidatedMessage validated) {
        if (existing == null || !senderRole.value().equals(existing.getSenderRole())
                || !senderRole.peer().value().equals(existing.getReceiverRole())
                || !validated.messageType().equals(existing.getMessageType())
                || !validated.content().equals(existing.getContent())
                || !Objects.equals(validated.replyToMessageId(), existing.getReplyToMessageId())) {
            throw new DuduPlanApiException(HttpStatus.CONFLICT, "message_id_conflict");
        }
    }

    private List<String> validateMessageIds(List<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty() || messageIds.size() > 100) {
            throw new DuduPlanApiException(HttpStatus.BAD_REQUEST, "invalid_message_ids");
        }
        LinkedHashSet<String> uniqueIds = new LinkedHashSet<>();
        try {
            for (String messageId : messageIds) {
                UUID.fromString(messageId);
                uniqueIds.add(messageId);
            }
        } catch (Exception exception) {
            throw new DuduPlanApiException(HttpStatus.BAD_REQUEST, "invalid_message_ids");
        }
        return List.copyOf(uniqueIds);
    }

    private record ValidatedMessage(String messageId, String messageType, String content,
                                    String replyToMessageId, long clientCreatedAt) {
    }
}
