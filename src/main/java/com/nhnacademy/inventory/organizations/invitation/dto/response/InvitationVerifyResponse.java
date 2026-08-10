package com.nhnacademy.inventory.organizations.invitation.dto.response;

public record InvitationVerifyResponse(
        String email,
        String organizationName
) {
}
