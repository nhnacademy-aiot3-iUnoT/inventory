package com.nhnacademy.inventory.chatbot.dto.response;

import java.util.List;

public record AssistantNoteChatResponse(
        long unreadCount,
        List<Note> notes
) {
    public record Note(
            String severity,
            String message,
            String createdAt
    ) {}
}
