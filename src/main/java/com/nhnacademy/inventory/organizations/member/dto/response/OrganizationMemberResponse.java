package com.nhnacademy.inventory.organizations.member.dto.response;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import java.time.LocalDateTime;

public record OrganizationMemberResponse(
        Long memberId,
        String email,
        OrganizationRole role,
        LocalDateTime joinedAt
){
}
