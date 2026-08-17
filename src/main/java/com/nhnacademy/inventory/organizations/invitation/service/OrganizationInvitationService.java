package com.nhnacademy.inventory.organizations.invitation.service;

import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationCreateRequest;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.organization.exception.OrganizationNotActiveException;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationInvitationService {

    private final OrganizationAccessService orgAccessService;
    private final InvitationService invitationService;

    @Transactional
    public void inviteMember(InvitationCreateRequest request) {
        Organization organization = orgAccessService.requireOwnerOrBossOrganization();

        if (organization.getStatus() == OrganizationStatus.PENDING) {
            throw new OrganizationNotActiveException();
        }

        invitationService.createInvitation(
                organization,
                request.email(),
                false
        );

        log.info("멤버({}) 초대 생성", request.email());
    }
}
