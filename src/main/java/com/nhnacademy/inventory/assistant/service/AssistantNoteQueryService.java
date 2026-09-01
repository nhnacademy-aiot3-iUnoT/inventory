package com.nhnacademy.inventory.assistant.service;

import com.nhnacademy.inventory.assistant.domain.AssistantNote;
import com.nhnacademy.inventory.assistant.dto.AssistantNoteListResponse;
import com.nhnacademy.inventory.assistant.dto.AssistantNoteResponse;
import com.nhnacademy.inventory.assistant.exception.AssistantNoteNotFoundException;
import com.nhnacademy.inventory.assistant.repository.AssistantNoteRepository;
import com.nhnacademy.inventory.global.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssistantNoteQueryService {

    private final AssistantNoteRepository assistantNoteRepository;

    @Transactional(readOnly = true)
    public AssistantNoteListResponse getNotes(boolean unreadOnly) {
        UUID accountUuid = UserContext.getUserUuid();

        List<AssistantNote> notes = unreadOnly
                ? assistantNoteRepository
                        .findAllByOrganizationMemberAccountUuidAndReadFalseOrderByCreatedAtDesc(accountUuid)
                : assistantNoteRepository
                        .findTop20ByOrganizationMemberAccountUuidOrderByCreatedAtDesc(accountUuid);

        return new AssistantNoteListResponse(
                assistantNoteRepository.countByOrganizationMemberAccountUuidAndReadFalse(accountUuid),
                notes.stream().map(AssistantNoteResponse::from).toList());
    }

    @Transactional
    public void markRead(Long noteId) {
        AssistantNote note = assistantNoteRepository
                .findByIdAndOrganizationMemberAccountUuid(noteId, UserContext.getUserUuid())
                .orElseThrow(AssistantNoteNotFoundException::new);

        note.markRead();
    }
}
