package com.nhnacademy.inventory.assistant.dto;

import com.nhnacademy.inventory.assistant.domain.AssistantNote;
import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.domain.StockOperation;
import com.nhnacademy.inventory.assistant.rule.FindingType;
import com.nhnacademy.inventory.assistant.domain.TargetType;

import java.time.LocalDateTime;

public record AssistantNoteResponse(
        Long noteId,
        StockOperation operation,
        Severity severity,
        FindingType findingType,
        String subject,
        String subjectDetail,
        String message,
        TargetType targetType,
        Long targetStorageId,
        Long targetId,
        boolean read,
        LocalDateTime createdAt
) {
    public static AssistantNoteResponse from(AssistantNote note) {
        return new AssistantNoteResponse(
                note.getId(),
                note.getOperation(),
                note.getSeverity(),
                note.getFindingType(),
                note.getSubject(),
                note.getSubjectDetail(),
                note.getMessage(),
                note.getTargetType(),
                note.getTargetStorageId(),
                note.getTargetId(),
                note.isRead(),
                note.getCreatedAt());
    }
}
