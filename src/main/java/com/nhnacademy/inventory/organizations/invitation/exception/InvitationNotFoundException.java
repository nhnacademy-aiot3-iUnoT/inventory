package com.nhnacademy.inventory.organizations.invitation.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class InvitationNotFoundException extends BaseException {
    public InvitationNotFoundException() {
        super(OrganizationErrorCode.INVITATION_NOT_FOUND);
    }
}
