package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;

public class ItemCodeRequiredException extends BaseException {
    public ItemCodeRequiredException(ErrorCode errorCode) {
        super(errorCode);
    }
}
