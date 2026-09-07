package com.nhnacademy.inventory.assistant.service;

import com.nhnacademy.inventory.assistant.domain.AssistantNote;
import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.domain.StockOperation;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import com.nhnacademy.inventory.assistant.repository.AssistantNoteRepository;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.FindingType;
import com.nhnacademy.inventory.assistant.rule.TargetReference;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssistantNoteServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long STORAGE_ID = 1L;
    private static final Long PACK_UNIT_ID = 3L;

    @Mock
    private AssistantNoteRepository assistantNoteRepository;

    @Mock
    private AssistantNarrator assistantNarrator;

    private AssistantNoteService service;

    private OrganizationMember member;

    @BeforeEach
    void setUp() {
        service = new AssistantNoteService(assistantNoteRepository, assistantNarrator);

        member = mock(OrganizationMember.class);
        given(member.getId()).willReturn(MEMBER_ID);

        given(assistantNarrator.describe(any())).willReturn("안내문");
        givenNoRecentNote();
    }

    @Test
    @DisplayName("판정 결과가 없으면 아무것도 저장하지 않는다.")
    void create_WhenNoFindings_SavesNothing() {
        service.create(member, StockOperation.INBOUND, List.of());

        then(assistantNoteRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("같은 대상의 판정은 하나의 알림으로 묶는다.")
    void create_GroupsFindingsByTarget() {
        service.create(member, StockOperation.INBOUND, List.of(
                finding(FindingType.EXPIRY_ORDER, Severity.WARN, target(PACK_UNIT_ID)),
                finding(FindingType.SCATTERED_STORAGE, Severity.INFO, target(PACK_UNIT_ID))));

        then(assistantNoteRepository).should().save(any(AssistantNote.class));
    }

    @Test
    @DisplayName("대상이 다르면 알림을 따로 만든다.")
    void create_SeparatesDifferentTargets() {
        service.create(member, StockOperation.INBOUND, List.of(
                finding(FindingType.EXPIRY_ORDER, Severity.WARN, target(PACK_UNIT_ID)),
                finding(FindingType.LOW_STOCK, Severity.WARN, target(99L))));

        then(assistantNoteRepository).should(org.mockito.Mockito.times(2)).save(any(AssistantNote.class));
    }

    @Test
    @DisplayName("묶인 판정 중 가장 높은 심각도를 알림의 심각도로 삼는다.")
    void create_UsesHighestSeverity() {
        service.create(member, StockOperation.INBOUND, List.of(
                finding(FindingType.SCATTERED_STORAGE, Severity.INFO, target(PACK_UNIT_ID)),
                finding(FindingType.STORAGE_CONDITION, Severity.CRITICAL, target(PACK_UNIT_ID)),
                finding(FindingType.EXPIRY_ORDER, Severity.WARN, target(PACK_UNIT_ID))));

        assertThat(savedNote().getSeverity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    @DisplayName("종류가 하나면 알림에 그 종류를 남긴다.")
    void create_WhenSingleType_KeepsFindingType() {
        service.create(member, StockOperation.INBOUND, List.of(
                finding(FindingType.EXPIRY_ORDER, Severity.WARN, target(PACK_UNIT_ID))));

        assertThat(savedNote().getFindingType()).isEqualTo(FindingType.EXPIRY_ORDER);
    }

    @Test
    @DisplayName("종류가 섞이면 대표 종류를 비운다.")
    void create_WhenMixedTypes_ClearsFindingType() {
        service.create(member, StockOperation.INBOUND, List.of(
                finding(FindingType.EXPIRY_ORDER, Severity.WARN, target(PACK_UNIT_ID)),
                finding(FindingType.SCATTERED_STORAGE, Severity.INFO, target(PACK_UNIT_ID))));

        assertThat(savedNote().getFindingType()).isNull();
    }

    @Test
    @DisplayName("같은 대상에 최근 알림이 있으면 다시 만들지 않는다.")
    void create_WhenRecentNoteExists_SkipsSave() {
        givenRecentNoteExists();

        service.create(member, StockOperation.INBOUND, List.of(
                finding(FindingType.EXPIRY_ORDER, Severity.WARN, target(PACK_UNIT_ID))));

        then(assistantNoteRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("대상을 알 수 없으면 중복을 판단하지 않고 저장한다.")
    void create_WhenTargetIsNull_SavesAnyway() {
        givenRecentNoteExists();

        service.create(member, StockOperation.INBOUND, List.of(
                finding(FindingType.EXPIRY_ORDER, Severity.WARN, null)));

        then(assistantNoteRepository).should().save(any(AssistantNote.class));
    }

    @Test
    @DisplayName("의약품명이 길어도 컬럼 길이에 맞춰 잘라 저장한다.")
    void create_TruncatesOverlongSubject() {
        // productName 은 500자까지 허용되는데 subject 컬럼은 300자임
        String longName = "가".repeat(400);

        service.create(member, StockOperation.INBOUND, List.of(
                new Finding(FindingType.EXPIRY_ORDER, Severity.WARN, longName,
                        "구역1 입고", "설명입니다.", target(PACK_UNIT_ID))));

        assertThat(savedNote().getSubject()).hasSize(300);
    }

    @Test
    @DisplayName("안내문이 길어도 컬럼 길이에 맞춰 잘라 저장한다.")
    void create_TruncatesOverlongMessage() {
        given(assistantNarrator.describe(any())).willReturn("나".repeat(1500));

        service.create(member, StockOperation.INBOUND, List.of(
                finding(FindingType.EXPIRY_ORDER, Severity.WARN, target(PACK_UNIT_ID))));

        assertThat(savedNote().getMessage()).hasSize(1000);
    }

    private AssistantNote savedNote() {
        ArgumentCaptor<AssistantNote> captor = ArgumentCaptor.forClass(AssistantNote.class);
        then(assistantNoteRepository).should().save(captor.capture());

        return captor.getValue();
    }

    private void givenNoRecentNote() {
        given(assistantNoteRepository
                .existsByOrganizationMemberIdAndOperationAndTargetTypeAndTargetStorageIdAndTargetIdAndCreatedAtAfter(
                        anyLong(), any(), any(), anyLong(), anyLong(), any(LocalDateTime.class)))
                .willReturn(false);
    }

    private void givenRecentNoteExists() {
        given(assistantNoteRepository
                .existsByOrganizationMemberIdAndOperationAndTargetTypeAndTargetStorageIdAndTargetIdAndCreatedAtAfter(
                        anyLong(), any(), any(), anyLong(), anyLong(), any(LocalDateTime.class)))
                .willReturn(true);
    }

    private Finding finding(FindingType type, Severity severity, TargetReference target) {
        return new Finding(type, severity, "타이레놀정", "구역1 입고", "설명입니다.", target);
    }

    private TargetReference target(Long targetId) {
        return new TargetReference(TargetType.PACK_UNIT, STORAGE_ID, targetId);
    }
}
