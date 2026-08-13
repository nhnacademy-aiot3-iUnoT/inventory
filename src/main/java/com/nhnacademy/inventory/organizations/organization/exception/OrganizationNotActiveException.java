package com.nhnacademy.inventory.organizations.organization.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class OrganizationNotActiveException extends BaseException {
    public OrganizationNotActiveException() {
        super(OrganizationErrorCode.ORG_NOT_ACTIVE);
    }
}
