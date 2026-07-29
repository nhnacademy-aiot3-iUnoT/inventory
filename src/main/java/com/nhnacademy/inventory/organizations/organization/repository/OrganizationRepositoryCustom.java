package com.nhnacademy.inventory.organizations.organization.repository;

import com.nhnacademy.inventory.organizations.organization.dto.request.OrgSearchRequest;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationRepositoryCustom {
    Page<OrgSearchResponse> search(OrgSearchRequest request, Pageable pageable);
}
