package com.nhnacademy.inventory.organizations.organization.dto.response;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;

import java.time.LocalDateTime;

public record OrgDetailResponse(
        Long id,
        String name,
        String roadAddress,
        String zipCode,
        String addressDetail,
        String description,
        OrganizationStatus status,
        LocalDateTime createdAt
) {
    public static OrgDetailResponse from(Organization organization) {
        return new OrgDetailResponse(
                organization.getId(),
                organization.getName(),
                organization.getRoadAddress(),
                organization.getZipCode(),
                organization.getAddressDetail(),
                organization.getDescription(),
                organization.getStatus(),
                organization.getCreatedAt()
        );
    }
}
