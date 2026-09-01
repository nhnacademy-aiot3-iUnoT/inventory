package com.nhnacademy.inventory.assistant.repository;

import com.nhnacademy.inventory.assistant.domain.AssistantNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssistantNoteRepository extends JpaRepository<AssistantNote, Long> {

    List<AssistantNote> findTop20ByOrganizationMemberAccountUuidOrderByCreatedAtDesc(UUID accountUuid);

    List<AssistantNote> findAllByOrganizationMemberAccountUuidAndReadFalseOrderByCreatedAtDesc(UUID accountUuid);

    long countByOrganizationMemberAccountUuidAndReadFalse(UUID accountUuid);

    Optional<AssistantNote> findByIdAndOrganizationMemberAccountUuid(Long id, UUID accountUuid);
}
