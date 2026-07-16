package org.xyjh.xyjhstartweb.duduplan.dto;

public record ChatMessageRequest(String messageId, String messageType, String content,
                                 String replyToMessageId, Long clientCreatedAt) {
}
