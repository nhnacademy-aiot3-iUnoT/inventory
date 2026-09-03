package com.nhnacademy.inventory.organizations.member.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class OrgBossLeaveNotAllowedException extends BaseException {
    public OrgBossLeaveNotAllowedException() {
        super(OrganizationErrorCode.ORG_BOSS_LEAVE_NOT_ALLOWED);
    }
}
