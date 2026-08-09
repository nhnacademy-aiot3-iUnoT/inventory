package com.nhnacademy.inventory.organizations.invitation.repository;

import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSearchRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvitationRepositoryCustom {
    Page<InvitationSearchResponse> search(Long organizationId, InvitationSearchRequest request, Pageable pageable);
}
