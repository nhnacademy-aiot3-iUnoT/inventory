package com.nhnacademy.inventory.organizations.organization.dto.response;

import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import java.time.LocalDateTime;

public record OrgSearchResponse(
        Long id,
        String businessNumber,
        String name,
        OrganizationStatus status,
        LocalDateTime createdAt
){
}
