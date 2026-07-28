package com.nhnacademy.inventory.organizations.storage.exception;

import com.nhnacademy.inventory.global.error.BaseException;

import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;


public class StorageNotFoundException extends BaseException {
    public StorageNotFoundException() {
        super(OrganizationErrorCode.STORAGE_NOT_FOUND);
    }
}
