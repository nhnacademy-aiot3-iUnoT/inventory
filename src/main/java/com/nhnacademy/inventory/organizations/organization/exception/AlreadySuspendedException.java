package com.nhnacademy.inventory.organizations.organization.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class AlreadySuspendedException extends BaseException {
    public AlreadySuspendedException() {
        super(OrganizationErrorCode.ORG_ALREADY_SUSPENDED);
    }
}
