package com.nhnacademy.inventory.organizations.storage.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;

public class StorageNameAlreadyExistsException extends BaseException {
    public StorageNameAlreadyExistsException() {
        super(ErrorCode.STORAGE_NAME_ALREADY_EXISTS);
    }
}
