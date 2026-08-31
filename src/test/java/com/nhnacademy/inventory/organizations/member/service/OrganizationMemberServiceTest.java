package com.nhnacademy.inventory.organizations.member.service;

import com.nhnacademy.inventory.global.client.AccountClient;
import com.nhnacademy.inventory.global.dto.account.AccountResponse;
import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.dto.request.OrganizationMemberSearchRequest;
import com.nhnacademy.inventory.organizations.member.dto.request.OrganizationRoleUpdateRequest;
import com.nhnacademy.inventory.organizations.member.dto.response.MemberOrganizationResponse;
import com.nhnacademy.inventory.organizations.member.dto.response.OrganizationMemberResponse;
import com.nhnacademy.inventory.organizations.member.exception.OrgMemberNotFoundException;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationDeletionService;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrganizationMemberServiceTest {

    @Mock
    private OrganizationMemberRepository orgMemberRepository;

    @Mock
    private AccountClient accountClient;

    @Mock
    private OrganizationDeletionService orgDeletionService;

    @Mock
    private OrganizationAccessService orgAccessService;

    @InjectMocks
    private OrganizationMemberService orgMemberService;

    private Organization organization;
    private OrganizationMember boss;
    private OrganizationMember owner;
    private OrganizationMember member;

    @BeforeEach
    void setUp() {
        organization = TestFixtures.createOrganization("테스트 조직", "123456789");
        ReflectionTestUtils.setField(organization, "id", 1L);

        boss = TestFixtures.createOrganizationBoss(organization);
        ReflectionTestUtils.setField(boss, "id", 1L);

        owner = TestFixtures.createOrganizationOwner(organization);
        ReflectionTestUtils.setField(owner, "id", 2L);

        member = TestFixtures.createOrganizationMember(organization);
        ReflectionTestUtils.setField(member, "id", 3L);
    }

    @Nested
    @DisplayName("부서 지정 조직원 목록 조회")
    class GetMembers {

        @Test
        @DisplayName("성공 - email 검색")
        void success_email_search() {
            UUID accountUuid = owner.getAccountUuid();
            Pageable pageable = PageRequest.of(0, 10);
            OrganizationMemberSearchRequest request = new OrganizationMemberSearchRequest("test", OrganizationRole.ORG_OWNER);
            AccountResponse accountResponse = new AccountResponse(accountUuid, "테스트 사용자", "test@email.com");
            Page<OrganizationMember> memberPage = new PageImpl<>(List.of(owner));

            given(orgAccessService.requireOwnerOrBoss()).willReturn(owner);
            given(accountClient.searchByEmail("test")).willReturn(List.of(accountResponse));
            given(orgMemberRepository.findMembers(organization.getId(), List.of(accountUuid), OrganizationRole.ORG_OWNER, true, pageable)).willReturn(memberPage);

            Page<OrganizationMemberResponse> response = orgMemberService.findMembers(request, true, pageable);

            assertEquals(1, response.getTotalElements());
            assertEquals("test@email.com", response.getContent().getFirst().email());
            assertEquals(OrganizationRole.ORG_OWNER, response.getContent().getFirst().role());

            verify(orgAccessService).requireOwnerOrBoss();
            verify(accountClient).searchByEmail("test");
            verify(orgMemberRepository).findMembers(organization.getId(), List.of(accountUuid), OrganizationRole.ORG_OWNER, true, pageable);
        }

        @Test
        @DisplayName("성공 - email 검색 없음")
        void success_without_email_search() {
            UUID accountUuid = owner.getAccountUuid();
            Pageable pageable = PageRequest.of(0, 10);
            OrganizationMemberSearchRequest request = new OrganizationMemberSearchRequest(null, OrganizationRole.ORG_OWNER);
            AccountResponse accountResponse = new AccountResponse(accountUuid, "테스트 사용자", "test@email.com");
            Page<OrganizationMember> memberPage = new PageImpl<>(List.of(owner));

            given(orgAccessService.requireOwnerOrBoss()).willReturn(owner);
            given(orgMemberRepository.findMembers(organization.getId(), null, OrganizationRole.ORG_OWNER, true, pageable)).willReturn(memberPage);
            given(accountClient.findByUuids(List.of(accountUuid))).willReturn(List.of(accountResponse));

            Page<OrganizationMemberResponse> response = orgMemberService.findMembers(request, true, pageable);

            assertEquals(1, response.getTotalElements());
            assertEquals("test@email.com", response.getContent().getFirst().email());
            assertEquals(OrganizationRole.ORG_OWNER, response.getContent().getFirst().role());

            verify(orgAccessService).requireOwnerOrBoss();
            verify(accountClient).findByUuids(List.of(accountUuid));
            verify(accountClient, never()).searchByEmail(anyString());
            verify(orgMemberRepository).findMembers(organization.getId(), null, OrganizationRole.ORG_OWNER, true, pageable);
        }

        @Test
        @DisplayName("성공 - email 검색 결과 없음")
        void success_email_search_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            OrganizationMemberSearchRequest request = new OrganizationMemberSearchRequest("test", OrganizationRole.ORG_OWNER);

            given(orgAccessService.requireOwnerOrBoss()).willReturn(owner);
            given(accountClient.searchByEmail("test")).willReturn(List.of());

            Page<OrganizationMemberResponse> response = orgMemberService.findMembers(request, true, pageable);

            assertTrue(response.isEmpty());

            verify(orgAccessService).requireOwnerOrBoss();
            verify(accountClient).searchByEmail("test");
            verify(orgMemberRepository, never()).findMembers(anyLong(), any(), any(), anyBoolean(), any(Pageable.class));
        }

        @Test
        @DisplayName("실패 - OWNER, BOSS가 아님")
        void forbidden() {
            Pageable pageable = PageRequest.of(0, 10);
            OrganizationMemberSearchRequest request = new OrganizationMemberSearchRequest(null, OrganizationRole.ORG_OWNER);

            given(orgAccessService.requireOwnerOrBoss()).willThrow(new ForbiddenException());

            assertThrows(ForbiddenException.class, () -> orgMemberService.findMembers(request, true, pageable));

            verify(orgAccessService).requireOwnerOrBoss();
            verify(accountClient, never()).searchByEmail(anyString());
            verify(orgMemberRepository, never()).findMembers(anyLong(), any(), any(), anyBoolean(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("부서 미지정 조직원 목록 조회")
    class GetMembersWithoutDepartment {

        @Test
        @DisplayName("성공 - email 검색")
        void success_email_search() {
            UUID accountUuid = owner.getAccountUuid();
            Pageable pageable = PageRequest.of(0, 10);
            OrganizationMemberSearchRequest request = new OrganizationMemberSearchRequest("test", null);
            AccountResponse accountResponse = new AccountResponse(accountUuid, "테스트 사용자", "test@email.com");
            Page<OrganizationMember> memberPage = new PageImpl<>(List.of(owner));

            given(orgAccessService.requireOwnerOrBoss()).willReturn(owner);
            given(accountClient.searchByEmail("test")).willReturn(List.of(accountResponse));
            given(orgMemberRepository.findMembers(organization.getId(), List.of(accountUuid), null, false, pageable)).willReturn(memberPage);

            Page<OrganizationMemberResponse> response = orgMemberService.findMembers(request, false, pageable);

            assertEquals(1, response.getTotalElements());
            assertEquals("test@email.com", response.getContent().getFirst().email());
            assertEquals(OrganizationRole.ORG_OWNER, response.getContent().getFirst().role());

            verify(orgAccessService).requireOwnerOrBoss();
            verify(accountClient).searchByEmail("test");
            verify(orgMemberRepository).findMembers(organization.getId(), List.of(accountUuid), null, false, pageable);
        }

        @Test
        @DisplayName("성공 - email 검색 없음")
        void success_without_email_search() {
            UUID accountUuid = owner.getAccountUuid();
            Pageable pageable = PageRequest.of(0, 10);
            OrganizationMemberSearchRequest request = new OrganizationMemberSearchRequest(null, null);
            AccountResponse accountResponse = new AccountResponse(accountUuid, "테스트 사용자", "test@email.com");
            Page<OrganizationMember> memberPage = new PageImpl<>(List.of(owner));

            given(orgAccessService.requireOwnerOrBoss()).willReturn(owner);
            given(orgMemberRepository.findMembers(organization.getId(), null, null, false, pageable)).willReturn(memberPage);
            given(accountClient.findByUuids(List.of(accountUuid))).willReturn(List.of(accountResponse));

            Page<OrganizationMemberResponse> response = orgMemberService.findMembers(request, false, pageable);

            assertEquals(1, response.getTotalElements());
            assertEquals("test@email.com", response.getContent().getFirst().email());
            assertEquals(OrganizationRole.ORG_OWNER, response.getContent().getFirst().role());

            verify(orgAccessService).requireOwnerOrBoss();
            verify(accountClient).findByUuids(List.of(accountUuid));
            verify(accountClient, never()).searchByEmail(anyString());
            verify(orgMemberRepository).findMembers(organization.getId(), null, null, false, pageable);
        }

        @Test
        @DisplayName("성공 - email 검색 결과 없음")
        void success_email_search_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            OrganizationMemberSearchRequest request = new OrganizationMemberSearchRequest("test", null);

            given(orgAccessService.requireOwnerOrBoss()).willReturn(owner);
            given(accountClient.searchByEmail("test")).willReturn(List.of());

            Page<OrganizationMemberResponse> response = orgMemberService.findMembers(request, false, pageable);

            assertTrue(response.isEmpty());

            verify(orgAccessService).requireOwnerOrBoss();
            verify(accountClient).searchByEmail("test");
            verify(orgMemberRepository, never()).findMembers(anyLong(), any(), any(), anyBoolean(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("조직원 Role 수정")
    class UpdateRole {

        @Test
        @DisplayName("성공 - OWNER로 변경")
        void success_owner() {
            OrganizationRoleUpdateRequest request = new OrganizationRoleUpdateRequest(OrganizationRole.ORG_OWNER);

            given(orgAccessService.requireBoss()).willReturn(boss);
            given(orgMemberRepository.findByIdAndOrganizationId(3L, organization.getId())).willReturn(Optional.of(member));

            orgMemberService.updateRole(3L, request);

            assertEquals(OrganizationRole.ORG_OWNER, member.getOrganizationRole());
            verify(orgAccessService).requireBoss();
            verify(orgMemberRepository).findByIdAndOrganizationId(3L, organization.getId());
        }

        @Test
        @DisplayName("성공 - MEMBER로 변경")
        void success_member() {
            OrganizationRoleUpdateRequest request = new OrganizationRoleUpdateRequest(OrganizationRole.ORG_MEMBER);

            given(orgAccessService.requireBoss()).willReturn(boss);
            given(orgMemberRepository.findByIdAndOrganizationId(2L, organization.getId())).willReturn(Optional.of(owner));

            orgMemberService.updateRole(2L, request);

            assertEquals(OrganizationRole.ORG_MEMBER, owner.getOrganizationRole());
            verify(orgAccessService).requireBoss();
            verify(orgMemberRepository).findByIdAndOrganizationId(2L, organization.getId());
        }

        @Test
        @DisplayName("성공 - 이미 OWNER인 조직원을 OWNER로 변경")
        void success_same_role() {
            OrganizationRoleUpdateRequest request = new OrganizationRoleUpdateRequest(OrganizationRole.ORG_OWNER);

            given(orgAccessService.requireBoss()).willReturn(boss);
            given(orgMemberRepository.findByIdAndOrganizationId(2L, organization.getId())).willReturn(Optional.of(owner));

            orgMemberService.updateRole(2L, request);

            assertEquals(OrganizationRole.ORG_OWNER, owner.getOrganizationRole());
            verify(orgAccessService).requireBoss();
        }

        @Test
        @DisplayName("실패 - BOSS가 아님")
        void forbidden() {
            OrganizationRoleUpdateRequest request = new OrganizationRoleUpdateRequest(OrganizationRole.ORG_OWNER);

            given(orgAccessService.requireBoss()).willThrow(new ForbiddenException());

            assertThrows(ForbiddenException.class, () -> orgMemberService.updateRole(2L, request));

            verify(orgAccessService).requireBoss();
            verify(orgMemberRepository, never()).findByIdAndOrganizationId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 조직원")
        void member_not_found() {
            OrganizationRoleUpdateRequest request = new OrganizationRoleUpdateRequest(OrganizationRole.ORG_MEMBER);

            given(orgAccessService.requireBoss()).willReturn(boss);
            given(orgMemberRepository.findByIdAndOrganizationId(2L, organization.getId())).willReturn(Optional.empty());

            assertThrows(OrgMemberNotFoundException.class, () -> orgMemberService.updateRole(2L, request));

            verify(orgAccessService).requireBoss();
            verify(orgMemberRepository).findByIdAndOrganizationId(2L, organization.getId());
        }
    }

    @Nested
    @DisplayName("조직원 삭제")
    class DeleteOrgMember {

        @Test
        @DisplayName("성공")
        void success() {
            given(orgAccessService.requireBoss()).willReturn(boss);
            given(orgMemberRepository.findByIdAndOrganizationId(3L, organization.getId())).willReturn(Optional.of(member));

            orgMemberService.deleteMember(3L);

            verify(orgAccessService).requireBoss();
            verify(orgMemberRepository).findByIdAndOrganizationId(3L, organization.getId());
            verify(orgDeletionService).deleteMember(member);
        }

        @Test
        @DisplayName("실패 - BOSS가 아님")
        void forbidden() {
            given(orgAccessService.requireBoss()).willThrow(new ForbiddenException());

            assertThrows(ForbiddenException.class, () -> orgMemberService.deleteMember(3L));

            verify(orgAccessService).requireBoss();
            verify(orgMemberRepository, never()).findByIdAndOrganizationId(anyLong(), anyLong());
            verify(orgDeletionService, never()).deleteMember(any());
        }

        @Test
        @DisplayName("실패 - 조직원이 존재하지 않음")
        void notFound() {
            given(orgAccessService.requireBoss()).willReturn(boss);
            given(orgMemberRepository.findByIdAndOrganizationId(4L, organization.getId())).willReturn(Optional.empty());

            assertThrows(OrgMemberNotFoundException.class, () -> orgMemberService.deleteMember(4L));

            verify(orgAccessService).requireBoss();
            verify(orgMemberRepository).findByIdAndOrganizationId(4L, organization.getId());
            verify(orgDeletionService, never()).deleteMember(any());
        }
    }

    @Nested
    @DisplayName("조직원 자진 탈퇴")
    class LeaveOrganization {

        @Test
        @DisplayName("성공")
        void success() {
            given(orgAccessService.getCurrentMember()).willReturn(member);

            orgMemberService.leaveOrganization();

            verify(orgAccessService).getCurrentMember();
            verify(orgDeletionService).deleteMember(member);
        }

        @Test
        @DisplayName("실패 - 조직에 속하지 않았음")
        void notFound() {
            given(orgAccessService.getCurrentMember()).willThrow(new UserOrgNotFoundException());

            assertThrows(UserOrgNotFoundException.class, () -> orgMemberService.leaveOrganization());

            verify(orgAccessService).getCurrentMember();
            verify(orgDeletionService, never()).deleteMember(any());
        }
    }

    @Nested
    @DisplayName("내부 서버 전용 - 조직 정보 조회")
    class GetMemberOrganization {

        @Test
        @DisplayName("성공")
        void success() {
            UUID accountUuid = owner.getAccountUuid();

            given(orgMemberRepository.findByAccountUuid(accountUuid)).willReturn(Optional.of(owner));

            MemberOrganizationResponse response = orgMemberService.getMemberOrganization(accountUuid);

            assertEquals(accountUuid, response.accountUuid());
            assertEquals(organization.getId(), response.organizationId());
            assertEquals(OrganizationRole.ORG_OWNER, response.organizationRole());

            verify(orgMemberRepository).findByAccountUuid(accountUuid);
        }

        @Test
        @DisplayName("실패 - 소속된 조직이 없음")
        void user_org_not_found() {
            UUID accountUuid = UUID.randomUUID();

            given(orgMemberRepository.findByAccountUuid(accountUuid)).willReturn(Optional.empty());

            assertThrows(UserOrgNotFoundException.class, () -> orgMemberService.getMemberOrganization(accountUuid));
        }
    }
}
