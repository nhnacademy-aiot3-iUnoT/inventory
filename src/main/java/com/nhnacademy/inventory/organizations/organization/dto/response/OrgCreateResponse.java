package com.nhnacademy.inventory.organizations.organization.dto.response;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;

public record OrgCreateResponse(
        Long id,
        String name
){
    public static OrgCreateResponse from(Organization organization) {
        return new OrgCreateResponse(
                organization.getId(),
                organization.getName()
        );
    }
}
