package com.nhnacademy.inventory.organizations.organization.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class OrgAlreadyCompletedException extends BaseException {
    public OrgAlreadyCompletedException() {
        super(OrganizationErrorCode.ORG_ALREADY_COMPLETE);
    }
}
