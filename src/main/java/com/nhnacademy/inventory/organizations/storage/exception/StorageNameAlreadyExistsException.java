package com.nhnacademy.inventory.organizations.storage.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;


public class StorageNameAlreadyExistsException extends BaseException {
    public StorageNameAlreadyExistsException() {
        super(OrganizationErrorCode.STORAGE_NAME_ALREADY_EXISTS);
    }
}
