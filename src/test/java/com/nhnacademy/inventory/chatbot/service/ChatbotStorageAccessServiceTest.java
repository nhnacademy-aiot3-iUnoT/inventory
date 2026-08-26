package com.nhnacademy.inventory.chatbot.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatbotStorageAccessServiceTest {
    @Mock
    private OrganizationMemberRepository memberRepository;

    @Mock
    private MemberDepartmentRepository memberDepartmentRepository;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private OrganizationMember member;

    @Mock
    private Organization organization;

    @InjectMocks
    private ChatbotStorageAccessService accessService;

    @BeforeEach
    void setup() {
        UserContext.setUserUuid(UUID.randomUUID());
    }

    @Nested
    @DisplayName("접근 가능한 저장소 조회")
    class GetAccessibleStorageIds {
        @Test
        @DisplayName("BOSS는 전체 저장소 반환")
        void success_boss() {
            given(memberRepository.findByAccountUuid(UserContext.getUserUuid())).willReturn(Optional.of(member));
            given(member.getOrganizationRole()).willReturn(OrganizationRole.ORG_BOSS);
            given(member.getOrganization()).willReturn(organization);
            given(organization.getId()).willReturn(1L);
            given(storageRepository.findIdsByOrganizationIdAndStatusNot(1L, StorageStatus.CLOSED))
                    .willReturn(List.of(100L, 200L));

            assertEquals(List.of(100L, 200L), accessService.getAccessibleStorageIds());
            verify(storageRepository).findIdsByOrganizationIdAndStatusNot(1L, StorageStatus.CLOSED);
            verify(memberDepartmentRepository, never()).findAccessibleStorageIds(1L);
        }

        @Test
        @DisplayName("조직원-부서-저장소 연결로 접근 저장소를 반환한다")
        void success() {
            given(memberRepository.findByAccountUuid(UserContext.getUserUuid())).willReturn(Optional.of(member));
            given(member.getId()).willReturn(10L);
            given(memberDepartmentRepository.findAccessibleStorageIds(10L)).willReturn(List.of(100L, 200L));

            assertEquals(List.of(100L, 200L), accessService.getAccessibleStorageIds());
            verify(memberDepartmentRepository).findAccessibleStorageIds(10L);
        }
    }
}
