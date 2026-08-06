package com.nhnacademy.inventory.organizations.invitation.dto.response;

import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;

import java.time.LocalDateTime;

public record InvitationSearchResponse(
        Long id,
        String email,
        InvitationStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiredAt
) {
}
