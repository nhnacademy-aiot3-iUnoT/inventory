package com.nhnacademy.inventory.organizations.invitation.event;

import java.util.UUID;

public record OwnerInvitationCreatedEvent (
        String email,
        UUID token
){
}
