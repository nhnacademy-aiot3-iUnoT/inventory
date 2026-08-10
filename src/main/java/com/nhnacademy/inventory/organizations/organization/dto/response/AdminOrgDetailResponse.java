package com.nhnacademy.inventory.organizations.organization.dto.response;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;

import java.time.LocalDateTime;

public record AdminOrgDetailResponse(
        Long id,
        String businessNumber,
        String name,
        String roadAddress,
        String zipCode,
        String addressDetail,
        OrganizationStatus status,
        LocalDateTime createdAt
){
    public static AdminOrgDetailResponse from(Organization organization) {
        return new AdminOrgDetailResponse(
                organization.getId(),
                organization.getBusinessNumber(),
                organization.getName(),
                organization.getRoadAddress(),
                organization.getZipCode(),
                organization.getAddressDetail(),
                organization.getStatus(),
                organization.getCreatedAt()
        );
    }
}
