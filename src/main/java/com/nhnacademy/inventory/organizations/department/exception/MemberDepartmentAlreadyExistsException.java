package com.nhnacademy.inventory.organizations.department.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class MemberDepartmentAlreadyExistsException extends BaseException {
    public MemberDepartmentAlreadyExistsException() {
        super(OrganizationErrorCode.MEMBER_DEPARTMENT_ALREADY_EXISTS);
    }
}
