package com.nhnacademy.inventory.assistant.service;

import com.nhnacademy.inventory.assistant.domain.AssistantNote;
import com.nhnacademy.inventory.assistant.dto.AssistantNoteListResponse;
import com.nhnacademy.inventory.assistant.dto.AssistantNoteResponse;
import com.nhnacademy.inventory.assistant.exception.AssistantNoteNotFoundException;
import com.nhnacademy.inventory.assistant.repository.AssistantNoteRepository;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
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
        UUID accountUuid = requireAccountUuid();

        List<AssistantNote> notes = unreadOnly
                ? assistantNoteRepository
                        .findTop20ByOrganizationMemberAccountUuidAndReadFalseOrderByCreatedAtDesc(accountUuid)
                : assistantNoteRepository
                        .findTop20ByOrganizationMemberAccountUuidOrderByCreatedAtDesc(accountUuid);

        return new AssistantNoteListResponse(
                assistantNoteRepository.countByOrganizationMemberAccountUuidAndReadFalse(accountUuid),
                notes.stream().map(AssistantNoteResponse::from).toList());
    }

    @Transactional
    public void markRead(Long noteId) {
        AssistantNote note = assistantNoteRepository
                .findByIdAndOrganizationMemberAccountUuid(noteId, requireAccountUuid())
                .orElseThrow(AssistantNoteNotFoundException::new);

        note.markRead();
    }

    private UUID requireAccountUuid() {
        UUID accountUuid = UserContext.getUserUuid();

        if (accountUuid == null) {
            throw new UserOrgNotFoundException();
        }

        return accountUuid;
    }
}
