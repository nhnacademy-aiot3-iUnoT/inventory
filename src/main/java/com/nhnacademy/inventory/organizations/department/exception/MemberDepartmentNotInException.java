package com.nhnacademy.inventory.organizations.department.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class MemberDepartmentNotInException extends BaseException {
    public MemberDepartmentNotInException() {
        super(OrganizationErrorCode.MEMBER_DEPARTMENT_NOT_IN);
    }
}
