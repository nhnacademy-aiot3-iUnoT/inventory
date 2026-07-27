package com.nhnacademy.inventory.organizations.storage.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;

public class StorageForbiddenException extends BaseException {
    public StorageForbiddenException() {
        super(ErrorCode.FORBIDDEN);
    }
}
