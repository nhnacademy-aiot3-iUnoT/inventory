package com.nhnacademy.inventory.organizations.invitation.service;

import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationType;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationCreateRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationCreateResponse;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationInvitationService {
    private final OrganizationService organizationService;
    private final InvitationService invitationService;

    @Transactional
    public InvitationCreateResponse inviteMember(InvitationCreateRequest request) {
        Organization organization = organizationService.getOrgAfterValidateOwner();

        Invitation invitation = invitationService.createInvitation(
                        organization,
                        request.email(),
                        InvitationType.MEMBER
                );

        return InvitationCreateResponse.from(invitation);
    }
}
