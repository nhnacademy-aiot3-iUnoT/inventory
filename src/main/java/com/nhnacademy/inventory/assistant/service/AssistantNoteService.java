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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantNoteService {

    private static final int MESSAGE_MAX_LENGTH = 1000;

    private final AssistantNoteRepository assistantNoteRepository;
    private final AssistantNarrator assistantNarrator;

    @Transactional
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

    private void save(OrganizationMember member, StockOperation operation,
                      TargetReference target, List<Finding> findings) {
        Finding highest = highestSeverity(findings);

        assistantNoteRepository.save(AssistantNote.of(
                member,
                operation,
                highest.severity(),
                commonType(findings),
                highest.subject(),
                highest.detail(),
                truncate(assistantNarrator.describe(findings)),
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

    private String truncate(String message) {
        return message.length() <= MESSAGE_MAX_LENGTH
                ? message
                : message.substring(0, MESSAGE_MAX_LENGTH);
    }
}
