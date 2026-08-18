package com.nhnacademy.inventory.organizations.member.dto.request;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;

// front -> inventory
public record OrganizationMemberSearchRequest (
        String email,
        OrganizationRole role
){
}
