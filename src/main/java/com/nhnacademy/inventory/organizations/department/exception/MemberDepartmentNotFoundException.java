package com.nhnacademy.inventory.organizations.department.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class MemberDepartmentNotFoundException extends BaseException {
    public MemberDepartmentNotFoundException() {
        super(OrganizationErrorCode.MEMBER_DEPARTMENT_NOT_FOUND);
    }
}
