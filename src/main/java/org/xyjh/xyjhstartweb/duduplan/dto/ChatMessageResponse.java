package org.xyjh.xyjhstartweb.duduplan.dto;

import org.xyjh.xyjhstartweb.duduplan.entity.DuduChatMessage;

public record ChatMessageResponse(Long id, String messageId, String senderRole, String receiverRole,
                                  String messageType, String content, String replyToMessageId,
                                  Long clientCreatedAt, Long serverCreatedAt, Long deliveredAt,
                                  Long readAt, Long recalledAt) {
    public static ChatMessageResponse from(DuduChatMessage message) {
        boolean recalled = message.getRecalledAt() != null;
        return new ChatMessageResponse(message.getId(), message.getMessageId(), message.getSenderRole(),
                message.getReceiverRole(), message.getMessageType(), recalled ? null : message.getContent(),
                recalled ? null : message.getReplyToMessageId(), message.getClientCreatedAt(),
                message.getServerCreatedAt(), message.getDeliveredAt(), message.getReadAt(), message.getRecalledAt());
    }
}
