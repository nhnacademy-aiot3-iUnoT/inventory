package com.nhnacademy.inventory.organizations.organization.dto.response;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminOwnerResponse(
        // TODO : accountUuid로 email 알아오기
        UUID accountUuid,
        LocalDateTime joinedAt
) {
    public static AdminOwnerResponse from(OrganizationMember member) {
        return new AdminOwnerResponse(
                member.getAccountUuid(),
                member.getJoinedAt()
        );
    }
}
