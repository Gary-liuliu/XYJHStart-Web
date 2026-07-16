package org.xyjh.xyjhstartweb.duduplan.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.xyjh.xyjhstartweb.duduplan.dto.ChatMessageRequest;
import org.xyjh.xyjhstartweb.duduplan.dto.ChatMessageResponse;
import org.xyjh.xyjhstartweb.duduplan.entity.DuduChatMessage;
import org.xyjh.xyjhstartweb.duduplan.error.DuduPlanApiException;
import org.xyjh.xyjhstartweb.duduplan.mapper.DuduChatMessageMapper;
import org.xyjh.xyjhstartweb.duduplan.realtime.DuduPlanRealtimeGateway;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanRole;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DuduPlanChatServiceTests {
    private DuduChatMessageMapper messageMapper;
    private DuduPlanChatService chatService;

    @BeforeEach
    void setUp() {
        messageMapper = mock(DuduChatMessageMapper.class);
        DuduPlanRealtimeGateway realtimeGateway = mock(DuduPlanRealtimeGateway.class);
        chatService = new DuduPlanChatService(messageMapper, realtimeGateway,
                new DuduPlanEncouragementRateLimiter());
    }

    @Test
    void saveMessageDerivesReceiverAndTrimsContent() {
        String messageId = UUID.randomUUID().toString();
        when(messageMapper.findByMessageId(messageId)).thenReturn(null);
        when(messageMapper.insert(any())).thenAnswer(invocation -> {
            DuduChatMessage message = invocation.getArgument(0);
            message.setId(7L);
            return 1;
        });

        ChatMessageResponse response = chatService.saveMessage(DuduPlanRole.OWNER,
                new ChatMessageRequest(messageId, "text", "  你好 👋  ", null, 100L));

        ArgumentCaptor<DuduChatMessage> messageCaptor = ArgumentCaptor.forClass(DuduChatMessage.class);
        verify(messageMapper).insert(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getReceiverRole()).isEqualTo("observer");
        assertThat(response.content()).isEqualTo("你好 👋");
    }

    @Test
    void duplicateMessageIdWithChangedContentIsConflict() {
        String messageId = UUID.randomUUID().toString();
        DuduChatMessage existing = new DuduChatMessage();
        existing.setMessageId(messageId);
        existing.setSenderRole("owner");
        existing.setReceiverRole("observer");
        existing.setMessageType("text");
        existing.setContent("原消息");
        when(messageMapper.findByMessageId(messageId)).thenReturn(existing);

        assertThatThrownBy(() -> chatService.saveMessage(DuduPlanRole.OWNER,
                new ChatMessageRequest(messageId, "text", "修改后的消息", null, 100L)))
                .isInstanceOfSatisfying(DuduPlanApiException.class,
                        exception -> assertThat(exception.getError()).isEqualTo("message_id_conflict"));
    }

    @Test
    void recalledResponseNeverContainsOriginalContent() {
        DuduChatMessage recalled = new DuduChatMessage();
        recalled.setContent("敏感正文");
        recalled.setReplyToMessageId(UUID.randomUUID().toString());
        recalled.setRecalledAt(200L);

        ChatMessageResponse response = ChatMessageResponse.from(recalled);

        assertThat(response.content()).isNull();
        assertThat(response.replyToMessageId()).isNull();
    }
}
