package com.nhnacademy.inventory.organizations.organization.dto.request;

import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;

public record OrgSearchRequest (
        OrganizationStatus status,
        String name
){
}
