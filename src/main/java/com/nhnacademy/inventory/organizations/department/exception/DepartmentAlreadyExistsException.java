package com.nhnacademy.inventory.organizations.department.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class DepartmentAlreadyExistsException extends BaseException {
    public DepartmentAlreadyExistsException() {
        super(OrganizationErrorCode.DEPARTMENT_ALREADY_EXISTS);
    }
}
