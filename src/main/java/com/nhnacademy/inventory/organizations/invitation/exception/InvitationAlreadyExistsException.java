package com.nhnacademy.inventory.organizations.invitation.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class InvitationAlreadyExistsException extends BaseException {
    public InvitationAlreadyExistsException() {
        super(OrganizationErrorCode.INVITATION_ALREADY_EXISTS);
    }
}
