package com.nhnacademy.inventory.organizations.invitation.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class InvalidInvitationException extends BaseException {
    public InvalidInvitationException() {
        super(OrganizationErrorCode.INVITATION_INVALID);
    }
}
