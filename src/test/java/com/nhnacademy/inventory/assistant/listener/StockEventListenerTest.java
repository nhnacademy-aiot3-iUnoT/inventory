package com.nhnacademy.inventory.assistant.listener;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.domain.StockOperation;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import com.nhnacademy.inventory.assistant.event.StockInboundCompletedEvent;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.FindingType;
import com.nhnacademy.inventory.assistant.rule.TargetReference;
import com.nhnacademy.inventory.assistant.rule.inbound.InboundRule;
import com.nhnacademy.inventory.assistant.service.AssistantNoteService;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StockEventListenerTest {

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private AssistantNoteService assistantNoteService;

    @BeforeEach
    void setUp() {
        Organization organization = mock(Organization.class);
        given(organization.getId()).willReturn(3L);

        OrganizationMember member = mock(OrganizationMember.class);
        given(member.getOrganization()).willReturn(organization);

        given(organizationMemberRepository.findByAccountUuid(any())).willReturn(Optional.of(member));
    }

    @Test
    @DisplayName("규칙 하나가 실패해도 나머지 판정은 알림으로 만든다.")
    void handleInbound_WhenOneRuleFails_KeepsOtherFindings() {
        InboundRule failing = (event, organizationId) -> {
            throw new IllegalStateException("규칙 오류");
        };
        InboundRule working = (event, organizationId) -> Optional.of(finding());

        listener(List.of(failing, working)).handleInbound(inboundEvent());

        assertThat(capturedFindings()).hasSize(1);
    }

    @Test
    @DisplayName("모든 규칙이 실패해도 예외를 밖으로 던지지 않는다.")
    void handleInbound_WhenAllRulesFail_DoesNotThrow() {
        InboundRule failing = (event, organizationId) -> {
            throw new IllegalStateException("규칙 오류");
        };

        assertThatCode(() -> listener(List.of(failing, failing)).handleInbound(inboundEvent()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("조직원을 찾지 못하면 알림을 만들지 않는다.")
    void handleInbound_WhenMemberNotFound_SkipsCreation() {
        given(organizationMemberRepository.findByAccountUuid(any())).willReturn(Optional.empty());

        listener(List.of((event, organizationId) -> Optional.of(finding()))).handleInbound(inboundEvent());

        then(assistantNoteService).should(never()).create(any(), any(), any());
    }

    private StockEventListener listener(List<InboundRule> inboundRules) {
        return new StockEventListener(
                inboundRules, List.of(), organizationMemberRepository, assistantNoteService);
    }

    @SuppressWarnings("unchecked")
    private List<Finding> capturedFindings() {
        ArgumentCaptor<List<Finding>> captor = ArgumentCaptor.forClass(List.class);
        then(assistantNoteService).should().create(any(), any(StockOperation.class), captor.capture());

        return captor.getValue();
    }

    private Finding finding() {
        return new Finding(FindingType.EXPIRY_ORDER, Severity.WARN, "타이레놀정", "구역1 입고", "설명입니다.",
                new TargetReference(TargetType.PACK_UNIT, 1L, 3L));
    }

    private StockInboundCompletedEvent inboundEvent() {
        return new StockInboundCompletedEvent(
                UUID.randomUUID(), 1L, 3L, LocalDate.now().plusMonths(6), 10);
    }
}
