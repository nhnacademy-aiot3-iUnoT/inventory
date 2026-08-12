package com.nhnacademy.inventory.organizations.organization.dto.response;

import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;

import java.time.LocalDateTime;

public record AdminInvitationResponse(
        Long id,
        String email,
        InvitationStatus status,
        boolean reissued,
        LocalDateTime createdAt,
        LocalDateTime expiredAt
) {
    public static AdminInvitationResponse from(Invitation invitation) {
        return new AdminInvitationResponse(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getInvitationStatus(),
                invitation.isReissued(),
                invitation.getCreatedAt(),
                invitation.getExpiredAt()
        );
    }
}
