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

    /**
     * 같은 작업으로 같은 대상에 대해 최근에 남긴 알림이 있는지 확인한다.
     * <p>
     * 같은 품목을 연달아 입고하면 같은 판정이 반복되는데, 사용자에게는 이미 본 알림이 또 쌓이는 것으로만 보인다.
     */
    boolean existsByOrganizationMemberIdAndOperationAndTargetTypeAndTargetStorageIdAndTargetIdAndCreatedAtAfter(
            Long organizationMemberId,
            StockOperation operation,
            TargetType targetType,
            Long targetStorageId,
            Long targetId,
            LocalDateTime createdAt);
}
