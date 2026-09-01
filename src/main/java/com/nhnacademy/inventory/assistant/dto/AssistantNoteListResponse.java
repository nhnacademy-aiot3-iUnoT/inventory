package com.nhnacademy.inventory.assistant.dto;

import java.util.List;

public record AssistantNoteListResponse(
        long unreadCount,
        List<AssistantNoteResponse> notes
) {
}
