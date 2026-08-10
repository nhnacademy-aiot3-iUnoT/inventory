package com.nhnacademy.inventory.organizations.organization.dto.request;

import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import jakarta.validation.constraints.NotNull;

public record OrgStatusUpdateRequest (
        @NotNull(message = "변경하고자 하는 조직 상태를 입력해주세요")
        OrganizationStatus status
){
}
