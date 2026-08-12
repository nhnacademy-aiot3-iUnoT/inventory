package com.nhnacademy.inventory.organizations.organization.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;

import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.organization.dto.request.*;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgCreateResponse;
import com.nhnacademy.inventory.organizations.organization.exception.OrgAlreadyExistsException;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private Organization organization;
    private OrganizationMember owner;
    private OrganizationMember member;

    @BeforeEach
    void setUp() {
        organization = TestFixtures.createOrganization("테스트 조직", "1234567890");

        owner = TestFixtures.createOrganizationMember(organization);
        owner.changeRole(OrganizationRole.ORG_OWNER);

        member = TestFixtures.createOrganizationMember(organization);
        member.changeRole(OrganizationRole.ORG_MEMBER);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("조직 생성 성공")
    void createOrganization_success() {
        OrgCreateRequest request = new OrgCreateRequest("1234567890", "test@test.com", "테스트 조직");

        given(organizationRepository.existsByBusinessNumber(anyString())).willReturn(false); // 중복 x

        Invitation invitation = mock(Invitation.class);

        given(invitationService.createInvitation(any(), anyString())).willReturn(invitation);

        OrgCreateResponse response = organizationService.createOrganization(request);

        assertEquals("테스트 조직", response.name());

        verify(organizationRepository).save(any(Organization.class));
        verify(invitationService).createInvitation(any(), eq(request.email()));
    }

    @Test
    @DisplayName("조직 생성 실패 - 사업자번호 중복")
    void createOrganization_duplicate() {
        OrgCreateRequest request = new OrgCreateRequest("1234567890", "test@test.com", "테스트 조직");

        given(organizationRepository.existsByBusinessNumber(anyString())).willReturn(true);

        assertThrows(OrgAlreadyExistsException.class,
                () -> organizationService.createOrganization(request));

        verify(organizationRepository, never()).save(any());
        verify(invitationService, never()).createInvitation(any(), anyString());
    }

    @Test
    @DisplayName("조직 초기화 성공")
    void completeOrganization_success() {
        UserContext.setUserUuid(owner.getAccountUuid());

        given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(owner);

        OrganizationSetupRequest request = new OrganizationSetupRequest("12345", "서울시 강남구", "101호", "테스트 조직");

        organizationService.setupOrganization(request);

        assertEquals(OrganizationStatus.ACTIVE, organization.getStatus());
    }

    @Test
    @DisplayName("조직 초기화 실패 - OWNER 아님")
    void setupOrganization_forbidden() {
        UserContext.setUserUuid(member.getAccountUuid());

        given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(member);

        OrganizationSetupRequest request = new OrganizationSetupRequest("12345", "주소", "상세", "설명");

        assertThrows(ForbiddenException.class, () -> organizationService.setupOrganization(request));
    }

    @Test
    @DisplayName("조직 상태 변경 성공")
    void updateOrganizationStatus_success() {

        // 상태 변경은 ACTIVE -> INACTIVE 만 가능
        organization.complete("12345", "서울시 강남구", "101호", "테스트 조직");

        UserContext.setUserUuid(owner.getAccountUuid());

        given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(owner);


        OrgStatusUpdateRequest request = new OrgStatusUpdateRequest(OrganizationStatus.INACTIVE);

        organizationService.updateOrganizationStatus(request);

        assertEquals(OrganizationStatus.INACTIVE, organization.getStatus());

    }

    @Test
    @DisplayName("조직 정보 수정 성공")
    void updateOrganization_success() {

        UserContext.setUserUuid(owner.getAccountUuid());

        given(organizationMemberService.getCurrentOrganizationMember(any())).willReturn(owner);

        OrgUpdateRequest request = new OrgUpdateRequest("도로명주소", "12345", "상세주소", "설명");

        organizationService.updateOrganization(request);

        assertEquals("도로명주소", organization.getRoadAddress());
        assertEquals("12345", organization.getZipCode());
    }

    @Test
    @DisplayName("조직 삭제 성공 - PENDING 상태 hard delete")
    void deleteOrganization_pending_success() {

        given(organizationRepository.findById(organization.getId()))
                .willReturn(Optional.of(organization));

        organizationService.deleteOrganization(organization.getId());

        verify(organizationRepository).delete(organization);
        verify(orgDeletionService, never()).softDelete(any());
    }

    @Test
    @DisplayName("조직 삭제 성공 - ACTIVE 상태 soft delete 위임")
    void deleteOrganization_active_success() {

        organization.complete(
                "12345",
                "서울시 강남구",
                "101호",
                "테스트 조직"
        );

        given(organizationRepository.findById(organization.getId())).willReturn(Optional.of(organization));

        organizationService.deleteOrganization(organization.getId());

        verify(orgDeletionService).softDelete(organization);
        verify(organizationRepository, never()).delete(any());
    }
}
