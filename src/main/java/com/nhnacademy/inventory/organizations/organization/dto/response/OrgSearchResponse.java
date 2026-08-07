package com.nhnacademy.inventory.organizations.organization.dto.response;

import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record OrgSearchResponse(
        Long id,
        String businessNumber,
        String name,
        OrganizationStatus status,
        LocalDateTime createdAt
){
    @QueryProjection
    public OrgSearchResponse {

    }
}
