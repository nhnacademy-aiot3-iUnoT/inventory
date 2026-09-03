package com.nhnacademy.inventory.assistant.service;

import com.nhnacademy.inventory.assistant.domain.AssistantNote;
import com.nhnacademy.inventory.assistant.repository.AssistantNoteRepository;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.TargetReference;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantNoteService {

    private static final int MESSAGE_MAX_LENGTH = 1000;

    private final AssistantNoteRepository assistantNoteRepository;
    private final AssistantNarrator assistantNarrator;

    // 여러 개의 판정 결과를 하나의 알림으로 묶기
    @Transactional
    public void create(OrganizationMember member, List<Finding> findings) {
        if (findings.isEmpty()) {
            return;
        }

        String message = truncate(assistantNarrator.describe(findings));
        Finding highest = highestSeverity(findings);
        TargetReference target = highest.target();

        assistantNoteRepository.save(AssistantNote.of(
                member,
                highest.severity(),
                message,
                target == null ? null : target.type(),
                target == null ? null : target.storageId(),
                target == null ? null : target.targetId()));

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
