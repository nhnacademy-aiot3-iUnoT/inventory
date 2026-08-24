package com.nhnacademy.inventory.organizations.department.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class StorageDepartmentNotFoundException extends BaseException {
    public StorageDepartmentNotFoundException() {
        super(OrganizationErrorCode.STORAGE_DEPARTMENT_NOT_FOUND);
    }
}
