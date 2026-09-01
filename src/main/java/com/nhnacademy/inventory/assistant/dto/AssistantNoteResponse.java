package com.nhnacademy.inventory.assistant.dto;

import com.nhnacademy.inventory.assistant.domain.AssistantNote;
import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.domain.TargetType;

import java.time.LocalDateTime;

public record AssistantNoteResponse(
        Long noteId,
        Severity severity,
        String message,
        TargetType targetType,
        Long targetId,
        boolean read,
        LocalDateTime createdAt
) {
    public static AssistantNoteResponse from(AssistantNote note) {
        return new AssistantNoteResponse(
                note.getId(),
                note.getSeverity(),
                note.getMessage(),
                note.getTargetType(),
                note.getTargetId(),
                note.isRead(),
                note.getCreatedAt());
    }
}
