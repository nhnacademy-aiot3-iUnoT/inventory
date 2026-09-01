package com.nhnacademy.inventory.assistant.listener;

import com.nhnacademy.inventory.assistant.event.StockInboundCompletedEvent;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.InboundRule;
import com.nhnacademy.inventory.assistant.service.AssistantNoteService;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventListener {

    private final List<InboundRule> inboundRules;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final AssistantNoteService assistantNoteService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInbound(StockInboundCompletedEvent event) {
        try {
            OrganizationMember member = organizationMemberRepository
                    .findByAccountUuid(event.actorUuid())
                    .orElse(null);

            if (member == null) {
                log.warn("[Assistant] 조직원을 찾지 못해 알림을 건너뜁니다. actorUuid={}", event.actorUuid());

                return;
            }

            Long organizationId = member.getOrganization().getId();

            List<Finding> findings = inboundRules.stream()
                    .map(rule -> rule.evaluate(event, organizationId))
                    .flatMap(Optional::stream)
                    .toList();

            assistantNoteService.create(member, findings);
        } catch (Exception e) {
            log.error("[Assistant] 입고 알림 생성 실패. zoneId={}", event.zoneId(), e);
        }
    }
}
