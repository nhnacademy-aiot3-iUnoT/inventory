package com.nhnacademy.inventory.organizations.invitation.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationCreateRequest;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.exception.OrganizationNotActiveException;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationService;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrganizationInvitationServiceTest {

    @Mock
    private OrganizationService organizationService;

    @Mock
    private InvitationService invitationService;

    @InjectMocks
    private OrganizationInvitationService organizationInvitationService;

    private Organization organization;
    private Invitation invitation;

    @BeforeEach
    void setUp() {
        organization = TestFixtures.createOrganization("테스트 조직", "1234567890");
        invitation = TestFixtures.createInvitationMember(organization, "test@test.com");
    }

    @Test
    @DisplayName("조직원 초대 성공")
    void inviteMember_success() {
        InvitationCreateRequest request = new InvitationCreateRequest("test@test.com");

        organization.complete("12345", "광주시 남구", "101호", "테스트 조직"); // ACTIVE 상태

        given(organizationService.getOrgAfterValidateOwner()).willReturn(organization);
        given(invitationService.createInvitation(organization, request.email(), false)).willReturn(invitation);

        organizationInvitationService.inviteMember(request);

        verify(organizationService).getOrgAfterValidateOwner();
        verify(invitationService).createInvitation(organization, request.email(), false);
    }

    @Test
    @DisplayName("조직원 초대 실패 - PENDING 조직")
    void inviteMember_pendingOrganization() {
        InvitationCreateRequest request = new InvitationCreateRequest("test@test.com");

        given(organizationService.getOrgAfterValidateOwner()).willReturn(organization); // 기본은 PENDING

        assertThrows(OrganizationNotActiveException.class, () -> organizationInvitationService.inviteMember(request));

        verify(invitationService, never())
                .createInvitation(any(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("조직원 초대 실패 - OWNER 아님")
    void inviteMember_organizationNotFound() {
        InvitationCreateRequest request = new InvitationCreateRequest("test@test.com");

        given(organizationService.getOrgAfterValidateOwner()).willThrow(new ForbiddenException());

        assertThrows(ForbiddenException.class, () -> organizationInvitationService.inviteMember(request));

        verify(invitationService, never()).createInvitation(any(), anyString(), eq(false));
    }
}
