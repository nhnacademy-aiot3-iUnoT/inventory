package com.nhnacademy.inventory.organizations.organization.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;

import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.organization.dto.request.*;
import com.nhnacademy.inventory.organizations.organization.dto.response.AdminOrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgCreateResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgSearchResponse;
import com.nhnacademy.inventory.organizations.organization.exception.*;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private InvitationService invitationService;

    @Mock
    private OrganizationMemberService organizationMemberService;

    @Mock
    private OrganizationDeletionService orgDeletionService;

    @InjectMocks
    private OrganizationService organizationService;

    @Mock
    private InvitationRepository invitationRepository;

    private Organization organization;
    private OrganizationMember boss;
    private OrganizationMember owner;
    private OrganizationMember member;

    @BeforeEach
    void setUp() {
        organization = TestFixtures.createOrganization("테스트 조직", "1234567890");

        boss = TestFixtures.createOrganizationBoss(organization);
        owner = TestFixtures.createOrganizationOwner(organization);
        member = TestFixtures.createOrganizationMember(organization);
    }

    @Nested
    @DisplayName("조직 생성")
    class CreateOrganizationTest {
        @Test
        @DisplayName("성공")
        void success() {
            OrgCreateRequest request = new OrgCreateRequest(
                    "1234567890",
                    "test@test.com",
                    "테스트 조직"
            );

            given(organizationRepository.existsByBusinessNumber(anyString())).willReturn(false); // 중복 x

            organizationService.createOrganization(request);

            verify(organizationRepository).save(any(Organization.class));
            verify(invitationService).createInvitation(any(Organization.class), eq("test@test.com"), eq(true));
        }

        @Test
        @DisplayName("실패 - 사업자번호 중복")
        void duplicate() {
            OrgCreateRequest request = new OrgCreateRequest("1234567890", "test@test.com", "테스트 조직");

            given(organizationRepository.existsByBusinessNumber(anyString())).willReturn(true);

            assertThrows(OrgAlreadyExistsException.class,
                    () -> organizationService.createOrganization(request));

            verify(organizationRepository, never()).save(any());
            verify(invitationService, never()).createInvitation(any(), anyString(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("조직 초기화")
    class SetupOrganizationTest {
        @Test
        @DisplayName("성공")
        void success() {
            UserContext.setUserUuid(boss.getAccountUuid());

            given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(boss);

            OrganizationSetupRequest request = new OrganizationSetupRequest(
                    "12345",
                    "광주시 남구",
                    "101호",
                    "이 조직은 테스트 조직이다.");

            organizationService.setupOrganization(request);

            assertEquals(OrganizationStatus.ACTIVE, organization.getStatus());
            assertEquals("12345", organization.getZipCode());
            assertEquals("광주시 남구", organization.getRoadAddress());
            assertEquals("이 조직은 테스트 조직이다.", organization.getDescription());
        }

        @Test
        @DisplayName("조직 초기화 실패 - BOSS 아님")
        void forbidden() {
            UserContext.setUserUuid(owner.getAccountUuid());

            given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(owner);

            OrganizationSetupRequest request = new OrganizationSetupRequest(
                    "12345",
                    "광주시 남구",
                    "101호",
                    "이 조직은 테스트 조직이다.");

            assertThrows(ForbiddenException.class, () -> organizationService.setupOrganization(request));
        }

        @Test
        @DisplayName("조직 초기화 실패 - PENDING 상태의 조직이 아님")
        void alreadySetup() {
            UserContext.setUserUuid(boss.getAccountUuid());

            organization.complete("12345", "광주시 남구", "101호", "조직을 이미 초기화된 상태로 바꾸는 중");

            given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(boss);

            OrganizationSetupRequest request = new OrganizationSetupRequest(
                    "12345",
                    "광주시 남구",
                    "101호",
                    "이 조직은 테스트 조직이다.");

            assertThrows(AlreadySetupOrganization.class, () -> organizationService.setupOrganization(request));
        }
    }

    @Nested
    @DisplayName("조직 조회")
    class GetOrganizationTest {
        @Test
        @DisplayName("목록 조회 성공")
        void getOrganizationList_success() {
            OrgSearchRequest request = new OrgSearchRequest(null, null);
            Pageable pageable = PageRequest.of(0, 10);

            OrgSearchResponse response = new OrgSearchResponse(
                    1L,
                    "1234567890",
                    "테스트 조직",
                    OrganizationStatus.ACTIVE,
                    LocalDateTime.now()
            );

            Page<OrgSearchResponse> expected = new PageImpl<>(List.of(response), pageable, 1);

            given(organizationRepository.search(request, pageable)).willReturn(expected);

            Page<OrgSearchResponse> result = organizationService.getOrganizationList(request, pageable);

            assertEquals(expected, result);
            verify(organizationRepository).search(request,pageable);


        }

        @Test
        @DisplayName("관리자 조직 단건 조회 성공")
        void getOrganizationForAdmin_success() {
            Long organizationId = organization.getId();

            Invitation bossInvitation = TestFixtures.createInvitationOwner(organization, "boss@test.com");

            given(organizationRepository.findById(organizationId)).willReturn(Optional.of(organization));
            given(invitationRepository.findFirstByOrganizationIdAndInvitedByAdminTrueOrderByCreatedAtDesc(organizationId))
                    .willReturn(Optional.of(bossInvitation));

            AdminOrgDetailResponse response = organizationService.getOrganizationForAdmin(organizationId);

            assertEquals(organization.getName(), response.name());

            assertNotNull(response.invitation());
            assertEquals("boss@test.com", response.invitation().email());

            verify(organizationRepository).findById(organizationId);
            verify(invitationRepository).findFirstByOrganizationIdAndInvitedByAdminTrueOrderByCreatedAtDesc(organizationId);
        }

        @Test
        @DisplayName("관리자 조직 단건 조회 실패 - 존재하지 않는 조직")
        void getOrganizationForAdmin_notFound() {
            Long organizationId = 1L;

            given(organizationRepository.findById(organizationId)).willReturn(Optional.empty());

            assertThrows(OrgNotFoundException.class,
                    () -> organizationService.getOrganizationForAdmin(organizationId));

            verify(organizationRepository).findById(organizationId);
        }

        @Test
        @DisplayName("관리자 조직 단건 조회 성공 - Boss 초대 없음")
        void getOrganizationForAdmin_withoutInvitation() {
            Long organizationId = organization.getId();

            given(organizationRepository.findById(organizationId)).willReturn(Optional.of(organization));

            given(invitationRepository.findFirstByOrganizationIdAndInvitedByAdminTrueOrderByCreatedAtDesc(organizationId)).willReturn(Optional.empty());

            AdminOrgDetailResponse response = organizationService.getOrganizationForAdmin(organizationId);

            assertNull(response.invitation());

            verify(organizationRepository).findById(organizationId);
            verify(invitationRepository).findFirstByOrganizationIdAndInvitedByAdminTrueOrderByCreatedAtDesc(organizationId);
        }

        @Test
        @DisplayName("유저 조직 단건 조회 성공")
        void getOrganizationForUser_success() {
            UserContext.setUserUuid(member.getAccountUuid());

            given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(member);

            OrgDetailResponse response = organizationService.getOrganizationForUser();

            assertEquals(organization.getName(), response.name());
            verify(organizationMemberService).getCurrentOrganizationMember(member.getAccountUuid());
        }
    }

    @Nested
    @DisplayName("조직 상태 변경")
    class UpdateOrganizationStatusTest {
        @BeforeEach
        void setUpBoss() {
            UserContext.setUserUuid(boss.getAccountUuid());
            given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(boss);
        }

        @Test
        @DisplayName("성공 ACTIVE -> INACTIVE")
        void updateOrganizationStatus_success() {
            organization.complete("12345", "광주시 남구", "101호", "테스트 조직");

            OrgStatusUpdateRequest request = new OrgStatusUpdateRequest(OrganizationStatus.INACTIVE);

            organizationService.updateOrganizationStatus(request);

            assertEquals(OrganizationStatus.INACTIVE, organization.getStatus());
        }

        @Test
        @DisplayName("조직 상태 변경 실패 - PENDING -> ACTIVE")
        void updateOrganizationStatus_invalidPending() {
            OrgStatusUpdateRequest request = new OrgStatusUpdateRequest(OrganizationStatus.ACTIVE);

            assertThrows(InvalidOrgStatusException.class, () -> organizationService.updateOrganizationStatus(request));
        }

        @Test
        @DisplayName("조직 상태 변경 실패 - SUSPENDED -> ..")
        void updateOrganizationStatus_suspended() {
            organization.suspended();

            OrgStatusUpdateRequest request = new OrgStatusUpdateRequest(OrganizationStatus.INACTIVE);

            assertThrows(InvalidOrgStatusException.class, () -> organizationService.updateOrganizationStatus(request));
        }

        @Test
        @DisplayName("조직 상태 변경 실패 - BOSS 아님")
        void updateOrganizationStatus_forbidden() {
            UserContext.setUserUuid(owner.getAccountUuid());
            given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(owner);

            organization.complete("12345", "광주시 남구", "101호", "테스트 조직");

            OrgStatusUpdateRequest request = new OrgStatusUpdateRequest(OrganizationStatus.INACTIVE);

            assertThrows(ForbiddenException.class, () -> organizationService.updateOrganizationStatus(request));
        }
    }

    @Nested
    @DisplayName("조직 정보 수정")
    class UpdateOrganizationTest {
        @Test
        @DisplayName("조직 정보 수정 성공")
        void success() {
            UserContext.setUserUuid(boss.getAccountUuid());

            given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(boss);

            OrgUpdateRequest request = new OrgUpdateRequest(
                    "도로명주소",
                    "12345",
                    "상세주소",
                    "설명"
            );

            organizationService.updateOrganization(request);

            assertEquals("도로명주소", organization.getRoadAddress());
            assertEquals("12345", organization.getZipCode());
        }

        @Test
        @DisplayName("조직 정보 수정 실패 - Boss 아님")
        void forbidden() {
            UserContext.setUserUuid(owner.getAccountUuid());
            given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(owner);

            OrgUpdateRequest request = new OrgUpdateRequest(
                    "도로명주소",
                    "12345",
                    "상세주소",
                    "설명"
            );
            assertThrows(ForbiddenException.class, () -> organizationService.updateOrganization(request));
        }
    }

    @Nested
    @DisplayName("조직 삭제")
    class DeleteOrganizationTest {
        @Test
        @DisplayName("조직 삭제 성공 - PENDING 상태 hard delete")
        void deleteOrganization_pending_success() {
            Long organizationId = organization.getId();

            given(organizationRepository.findById(organizationId)).willReturn(Optional.of(organization));

            organizationService.deleteOrganization(organizationId);

            verify(orgDeletionService).deletePendingRelations(organizationId);
            verify(organizationRepository).delete(organization);
            verify(orgDeletionService, never()).softDelete(any());
        }

        @Test
        @DisplayName("조직 삭제 성공 - ACTIVE 상태 soft delete 위임")
        void deleteOrganization_active_success() {
            Long organizationId = organization.getId();
            organization.complete("12345", "광주시 남구", "101호", "테스트 조직");

            given(organizationRepository.findById(organizationId)).willReturn(Optional.of(organization));

            organizationService.deleteOrganization(organizationId);

            verify(orgDeletionService).softDelete(organization);
            verify(orgDeletionService, never()).deletePendingRelations(anyLong());
            verify(organizationRepository, never()).delete(any());
        }

        @Test
        @DisplayName("조직 삭제 실패 - 조직 없음")
        void notFound() {
            Long organizationId = 999L;

            given(organizationRepository.findById(organizationId)).willReturn(Optional.empty());

            assertThrows(OrgNotFoundException.class, () -> organizationService.deleteOrganization(organizationId));

            verify(organizationRepository).findById(organizationId);
            verify(orgDeletionService, never()).deletePendingRelations(anyLong());
            verify(orgDeletionService, never()).softDelete(any());
            verify(organizationRepository, never()).delete(any());
        }
    }

}
