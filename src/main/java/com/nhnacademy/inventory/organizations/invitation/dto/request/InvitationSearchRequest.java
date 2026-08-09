package com.nhnacademy.inventory.organizations.invitation.dto.request;

import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;

public record InvitationSearchRequest(
        String email,
        InvitationStatus status
) {
}
