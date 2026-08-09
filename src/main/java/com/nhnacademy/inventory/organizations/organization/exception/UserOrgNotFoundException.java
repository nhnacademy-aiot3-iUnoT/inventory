package com.nhnacademy.inventory.organizations.organization.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class UserOrgNotFoundException extends BaseException {
    public UserOrgNotFoundException() {
        super(OrganizationErrorCode.USER_ORG_NOT_FOUND);
    }
}
