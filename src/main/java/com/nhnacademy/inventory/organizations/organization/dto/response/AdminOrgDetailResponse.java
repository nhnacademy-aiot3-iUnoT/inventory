package com.nhnacademy.inventory.organizations.organization.dto.response;

import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record AdminOrgDetailResponse(
        Long id,
        String businessNumber,
        String name,
        String roadAddress,
        String zipCode,
        String addressDetail,
        OrganizationStatus status,
        LocalDateTime createdAt,
        AdminInvitationResponse invitation,
        List<AdminOwnerResponse> owners
) {
    public static AdminOrgDetailResponse from(
            Organization organization,
            Invitation invitation,
            List<OrganizationMember> owners
    ) {
        return new AdminOrgDetailResponse(
                organization.getId(),
                organization.getBusinessNumber(),
                organization.getName(),
                organization.getRoadAddress(),
                organization.getZipCode(),
                organization.getAddressDetail(),
                organization.getStatus(),
                organization.getCreatedAt(),
                invitation == null ? null : AdminInvitationResponse.from(invitation),
                owners.stream().map(AdminOwnerResponse::from).toList()
        );
    }
}
