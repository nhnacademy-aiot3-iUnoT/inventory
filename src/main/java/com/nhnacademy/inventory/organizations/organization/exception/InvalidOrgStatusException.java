package com.nhnacademy.inventory.organizations.organization.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class InvalidOrgStatusException extends BaseException {
    public InvalidOrgStatusException() {
        super(OrganizationErrorCode.ORG_STATUS_INVALID);
    }
}
