package com.nhnacademy.inventory.organizations.invitation.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class InvitationExpiredException extends BaseException {
    public InvitationExpiredException() {
        super(OrganizationErrorCode.INVITATION_ALREADY_EXPIRED);
    }
}
