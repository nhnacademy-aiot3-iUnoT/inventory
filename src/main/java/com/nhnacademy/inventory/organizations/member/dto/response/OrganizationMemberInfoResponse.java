package com.nhnacademy.inventory.organizations.member.dto.response;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;

import java.time.LocalDateTime;

public record OrganizationMemberInfoResponse (
        Long id,
        OrganizationRole role,
        LocalDateTime joinedAt
        // TODO 정보 추가
) {
    public static OrganizationMemberInfoResponse from(OrganizationMember member) {
        return new OrganizationMemberInfoResponse(
                member.getId(),
                member.getOrganizationRole(),
                member.getJoinedAt()
        );
    }
}
