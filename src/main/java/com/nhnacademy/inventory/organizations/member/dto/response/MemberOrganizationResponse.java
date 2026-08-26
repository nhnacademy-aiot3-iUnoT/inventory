package com.nhnacademy.inventory.organizations.member.dto.response;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;

import java.util.UUID;

public record MemberOrganizationResponse(
        UUID accountUuid,
        Long organizationId,
        OrganizationRole organizationRole
) {
    public static MemberOrganizationResponse from(OrganizationMember member) {
        return new MemberOrganizationResponse(
                member.getAccountUuid(),
                member.getOrganization().getId(),
                member.getOrganizationRole()
        );
    }
}
