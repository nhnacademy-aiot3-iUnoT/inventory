package com.nhnacademy.inventory.organizations.member.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class OrgMemberNotFoundException extends BaseException {
    public OrgMemberNotFoundException() {
        super(OrganizationErrorCode.ORG_MEMBER_NOT_FOUND);
    }
}
