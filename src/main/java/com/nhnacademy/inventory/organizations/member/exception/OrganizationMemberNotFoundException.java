package com.nhnacademy.inventory.organizations.member.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class OrganizationMemberNotFoundException extends BaseException {
    public OrganizationMemberNotFoundException() {
        super(OrganizationErrorCode.ORGANIZATION_MEMBER_NOT_FOUND);
    }
}
