package com.nhnacademy.inventory.organizations.storage.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;

public class StorageNotFoundException extends BaseException {
    public StorageNotFoundException() {
        super(ErrorCode.STORAGE_NOT_FOUND);
    }
}
