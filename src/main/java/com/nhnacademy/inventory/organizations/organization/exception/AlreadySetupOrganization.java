package com.nhnacademy.inventory.organizations.organization.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class AlreadySetupOrganization extends BaseException {
    public AlreadySetupOrganization() {
        super(OrganizationErrorCode.ORG_ALREADY_SETUP);
    }
}
