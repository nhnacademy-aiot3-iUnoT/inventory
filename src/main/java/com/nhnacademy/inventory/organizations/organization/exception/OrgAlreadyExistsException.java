package com.nhnacademy.inventory.organizations.organization.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class OrgAlreadyExistsException extends BaseException {
    public OrgAlreadyExistsException() {
        super(OrganizationErrorCode.ORG_ALREADY_EXISTS);
    }
}
