package com.nhnacademy.inventory.assistant.listener;

import com.nhnacademy.inventory.assistant.domain.StockOperation;
import com.nhnacademy.inventory.assistant.event.StockInboundCompletedEvent;
import com.nhnacademy.inventory.assistant.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.inbound.InboundRule;
import com.nhnacademy.inventory.assistant.rule.outbound.OutboundRule;
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
import java.util.UUID;
import java.util.function.Function;
import java.util.function.LongFunction;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventListener {

    private final List<InboundRule> inboundRules;
    private final List<OutboundRule> outboundRules;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final AssistantNoteService assistantNoteService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInbound(StockInboundCompletedEvent event) {
        handle(event.actorUuid(), StockOperation.INBOUND, organizationId ->
                evaluate(inboundRules, rule -> rule.evaluate(event, organizationId)));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutbound(StockOutboundCompletedEvent event) {
        handle(event.actorUuid(), StockOperation.OUTBOUND, organizationId ->
                evaluate(outboundRules, rule -> rule.evaluate(event, organizationId)));
    }

    private void handle(UUID actorUuid, StockOperation operation, LongFunction<List<Finding>> evaluator) {
        try {
            OrganizationMember member = organizationMemberRepository
                    .findByAccountUuid(actorUuid)
                    .orElse(null);

            if (member == null) {
                log.warn("[Assistant] 조직원을 찾지 못해 알림을 건너뜁니다. actorUuid={}", actorUuid);

                return;
            }

            assistantNoteService.create(member, operation, evaluator.apply(member.getOrganization().getId()));
        } catch (Exception e) {
            log.error("[Assistant] {} 알림 생성 실패. actorUuid={}", operation, actorUuid, e);
        }
    }

    private <R> List<Finding> evaluate(List<R> rules, Function<R, Optional<Finding>> evaluation) {
        return rules.stream()
                .map(evaluation)
                .flatMap(Optional::stream)
                .toList();
    }
}
