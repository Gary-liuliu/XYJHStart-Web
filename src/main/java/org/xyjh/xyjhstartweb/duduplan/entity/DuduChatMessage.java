package org.xyjh.xyjhstartweb.duduplan.entity;

public class DuduChatMessage {
    private Long id;
    private String messageId;
    private String senderRole;
    private String receiverRole;
    private String messageType;
    private String content;
    private String replyToMessageId;
    private Long clientCreatedAt;
    private Long serverCreatedAt;
    private Long deliveredAt;
    private Long readAt;
    private Long recalledAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public String getReceiverRole() { return receiverRole; }
    public void setReceiverRole(String receiverRole) { this.receiverRole = receiverRole; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getReplyToMessageId() { return replyToMessageId; }
    public void setReplyToMessageId(String replyToMessageId) { this.replyToMessageId = replyToMessageId; }
    public Long getClientCreatedAt() { return clientCreatedAt; }
    public void setClientCreatedAt(Long clientCreatedAt) { this.clientCreatedAt = clientCreatedAt; }
    public Long getServerCreatedAt() { return serverCreatedAt; }
    public void setServerCreatedAt(Long serverCreatedAt) { this.serverCreatedAt = serverCreatedAt; }
    public Long getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Long deliveredAt) { this.deliveredAt = deliveredAt; }
    public Long getReadAt() { return readAt; }
    public void setReadAt(Long readAt) { this.readAt = readAt; }
    public Long getRecalledAt() { return recalledAt; }
    public void setRecalledAt(Long recalledAt) { this.recalledAt = recalledAt; }
}
