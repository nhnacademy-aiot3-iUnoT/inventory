package com.nhnacademy.inventory.organizations.invitation.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class InvitationEmailMismatchException extends BaseException {
    public InvitationEmailMismatchException() {
        super(OrganizationErrorCode.INVITATION_EMAIL_MIS_MISMATCH);
    }
}
