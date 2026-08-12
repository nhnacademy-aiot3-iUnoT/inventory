package com.nhnacademy.inventory.organizations.invitation.dto.response;

import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;

// TODO 필요 없을 것 같음
public record InvitationCreateResponse(
        Long id,
        String email
) {

    public static InvitationCreateResponse from(Invitation invitation) {
        return new InvitationCreateResponse(
                invitation.getId(),
                invitation.getEmail()
        );
    }
}
