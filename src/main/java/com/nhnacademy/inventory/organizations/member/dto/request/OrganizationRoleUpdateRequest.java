package com.nhnacademy.inventory.organizations.member.dto.request;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import jakarta.validation.constraints.NotNull;

public record OrganizationRoleUpdateRequest (
        @NotNull(message = "조직원의 역할은 필수 입력입니다.")
        OrganizationRole role
) {
}
