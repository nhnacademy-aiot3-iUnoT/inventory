package com.nhnacademy.inventory.assistant.service;

import com.nhnacademy.inventory.assistant.domain.AssistantNote;
import com.nhnacademy.inventory.assistant.domain.StockOperation;
import com.nhnacademy.inventory.assistant.repository.AssistantNoteRepository;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.FindingType;
import com.nhnacademy.inventory.assistant.rule.TargetReference;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantNoteService {

    private static final int SUBJECT_MAX_LENGTH = 300;
    private static final int DETAIL_MAX_LENGTH = 300;
    private static final int MESSAGE_MAX_LENGTH = 1000;

    // 같은 작업으로 같은 대상에 대해 이 시간 안에 남긴 알림이 있으면 다시 만들지 않는다.
    private static final Duration DUPLICATE_WINDOW = Duration.ofMinutes(10);

    private final AssistantNoteRepository assistantNoteRepository;
    private final AssistantNarrator assistantNarrator;

    public void create(OrganizationMember member, StockOperation operation, List<Finding> findings) {
        groupByTarget(findings).forEach((target, group) -> save(member, operation, target, group));
    }

    private Map<TargetReference, List<Finding>> groupByTarget(List<Finding> findings) {
        Map<TargetReference, List<Finding>> grouped = new LinkedHashMap<>();

        for (Finding finding : findings) {
            grouped.computeIfAbsent(finding.target(), key -> new ArrayList<>()).add(finding);
        }

        return grouped;
    }

    /**
     * 대상을 알 수 없는 알림은 중복을 판단할 기준이 없어 그대로 저장한다.
     */
    private boolean isDuplicate(OrganizationMember member, StockOperation operation, TargetReference target) {
        if (target == null || target.type() == null || target.storageId() == null || target.targetId() == null) {
            return false;
        }

        return assistantNoteRepository
                .existsByOrganizationMemberIdAndOperationAndTargetTypeAndTargetStorageIdAndTargetIdAndCreatedAtAfter(
                        member.getId(), operation, target.type(), target.storageId(), target.targetId(),
                        LocalDateTime.now().minus(DUPLICATE_WINDOW));
    }

    private void save(OrganizationMember member, StockOperation operation,
                      TargetReference target, List<Finding> findings) {
        if (isDuplicate(member, operation, target)) {
            return;
        }

        Finding highest = highestSeverity(findings);

        assistantNoteRepository.save(AssistantNote.of(
                member,
                operation,
                highest.severity(),
                commonType(findings),
                truncate(highest.subject(), SUBJECT_MAX_LENGTH),
                truncate(highest.detail(), DETAIL_MAX_LENGTH),
                truncate(assistantNarrator.describe(findings), MESSAGE_MAX_LENGTH),
                target == null ? null : target.type(),
                target == null ? null : target.storageId(),
                target == null ? null : target.targetId()));
    }

    // 종류가 섞이면 하나로 대표할 수 없으니 비워둔다.
    private FindingType commonType(List<Finding> findings) {
        return findings.stream().map(Finding::type).distinct().count() == 1
                ? findings.getFirst().type()
                : null;
    }

    private Finding highestSeverity(List<Finding> findings) {
        return findings.stream()
                .max(Comparator.comparing(finding -> finding.severity().getWeight()))
                .orElseThrow();
    }

    // 컬럼 길이를 넘으면 저장에 실패해 알림이 통째로 사라지므로 잘라서라도 남긴다.
    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}
