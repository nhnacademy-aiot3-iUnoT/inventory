package com.nhnacademy.inventory.organizations.member.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class OrgMemberRoleChangeNotAllowedException extends BaseException {
    public OrgMemberRoleChangeNotAllowedException() {
        super(OrganizationErrorCode.ORG_MEMBER_ROLE_CHANGE_NOT_ALLOWED);
    }
}
