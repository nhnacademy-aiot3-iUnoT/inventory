package com.nhnacademy.inventory.organizations.department.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class DepartmentNotFoundException extends BaseException {
    public DepartmentNotFoundException() {
        super(OrganizationErrorCode.DEPARTMENT_NOT_FOUND);
    }
}
