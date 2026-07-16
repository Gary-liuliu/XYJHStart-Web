package org.xyjh.xyjhstartweb.duduplan.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xyjh.xyjhstartweb.duduplan.dto.ChatMessageResponse;
import org.xyjh.xyjhstartweb.duduplan.dto.DeliveredRequest;
import org.xyjh.xyjhstartweb.duduplan.dto.ReadRequest;
import org.xyjh.xyjhstartweb.duduplan.security.DuduPlanPrincipal;
import org.xyjh.xyjhstartweb.duduplan.service.DuduPlanChatService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dudu-plan/chat")
public class DuduPlanChatController {
    private final DuduPlanChatService chatService;

    public DuduPlanChatController(DuduPlanChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/messages")
    public Map<String, Object> messages(@AuthenticationPrincipal DuduPlanPrincipal principal,
                                        @RequestParam(required = false) Long beforeId,
                                        @RequestParam(defaultValue = "50") int limit) {
        List<ChatMessageResponse> items = chatService.history(principal.role(), beforeId, limit);
        Long nextCursor = items.size() == Math.min(Math.max(limit, 1), 50)
                ? items.get(items.size() - 1).id() : null;
        return Map.of("items", items, "nextCursor", nextCursor == null ? "" : nextCursor);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal DuduPlanPrincipal principal) {
        return Map.of("unreadCount", chatService.unreadCount(principal.role()));
    }

    @PostMapping("/delivered")
    public Map<String, Object> delivered(@AuthenticationPrincipal DuduPlanPrincipal principal,
                                         @Valid @RequestBody DeliveredRequest request) {
        return Map.of("items", chatService.markDelivered(principal.role(), request.messageIds()));
    }

    @PostMapping("/read")
    public Map<String, Object> read(@AuthenticationPrincipal DuduPlanPrincipal principal,
                                    @Valid @RequestBody ReadRequest request) {
        return chatService.markRead(principal.role(), request.upToMessageId());
    }

    @PostMapping("/messages/{messageId}/recall")
    public ChatMessageResponse recall(@AuthenticationPrincipal DuduPlanPrincipal principal,
                                      @PathVariable String messageId) {
        return chatService.recall(principal.role(), messageId);
    }
}
