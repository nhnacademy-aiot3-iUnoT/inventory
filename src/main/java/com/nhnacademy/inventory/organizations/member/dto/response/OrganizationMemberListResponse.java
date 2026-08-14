package com.nhnacademy.inventory.organizations.member.dto.response;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;

import java.time.LocalDateTime;

public record OrganizationMemberListResponse (
        Long id,
        OrganizationRole role,
        LocalDateTime joinedAt
){
    public static OrganizationMemberListResponse from(OrganizationMember member) {
        return new OrganizationMemberListResponse(
                member.getId(),
                member.getOrganizationRole(),
                member.getJoinedAt()
        );
    }
}
