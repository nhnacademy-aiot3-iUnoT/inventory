package com.nhnacademy.inventory.organizations.invitation.dto.response;

import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;

import java.time.LocalDateTime;

public record InvitationListInfoResponse(
        Long id,
        String email,
        InvitationStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiredAt
) {
    public static InvitationListInfoResponse from(Invitation invitation) {
        return new InvitationListInfoResponse(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getInvitationStatus(),
                invitation.getCreatedAt(),
                invitation.getExpiredAt()
        );
    }
}
