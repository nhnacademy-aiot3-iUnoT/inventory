package com.nhnacademy.inventory.global.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.GlobalErrorCode;

public class ForbiddenException extends BaseException {
    public ForbiddenException() {
        super(GlobalErrorCode.FORBIDDEN);
    }
}
