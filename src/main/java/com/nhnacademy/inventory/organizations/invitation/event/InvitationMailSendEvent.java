package com.nhnacademy.inventory.organizations.invitation.event;

import java.util.UUID;

public record InvitationMailSendEvent(
        String email,
        UUID token
){
}
