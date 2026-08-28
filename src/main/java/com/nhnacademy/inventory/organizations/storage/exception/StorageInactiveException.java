package com.nhnacademy.inventory.organizations.storage.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class StorageInactiveException extends BaseException {
    public StorageInactiveException() {
        super(OrganizationErrorCode.STORAGE_INACTIVE);
    }
}
