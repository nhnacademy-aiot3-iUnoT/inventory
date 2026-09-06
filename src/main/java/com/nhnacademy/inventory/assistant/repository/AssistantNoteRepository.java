package com.nhnacademy.inventory.assistant.repository;

import com.nhnacademy.inventory.assistant.domain.AssistantNote;
import com.nhnacademy.inventory.assistant.domain.StockOperation;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssistantNoteRepository extends JpaRepository<AssistantNote, Long> {

    List<AssistantNote> findTop20ByOrganizationMemberAccountUuidOrderByCreatedAtDesc(UUID accountUuid);

    List<AssistantNote> findTop20ByOrganizationMemberAccountUuidAndReadFalseOrderByCreatedAtDesc(UUID accountUuid);

    long countByOrganizationMemberAccountUuidAndReadFalse(UUID accountUuid);

    Optional<AssistantNote> findByIdAndOrganizationMemberAccountUuid(Long id, UUID accountUuid);

    // 같은 작업, 같은 대상으로 최근에 남긴 알림이 있는지 확인함 (중복 억제용)
    boolean existsByOrganizationMemberIdAndOperationAndTargetTypeAndTargetStorageIdAndTargetIdAndCreatedAtAfter(
            Long organizationMemberId,
            StockOperation operation,
            TargetType targetType,
            Long targetStorageId,
            Long targetId,
            LocalDateTime createdAt);
}
