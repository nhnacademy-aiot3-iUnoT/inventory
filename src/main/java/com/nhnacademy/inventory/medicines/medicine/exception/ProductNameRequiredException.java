package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;

public class ProductNameRequiredException extends BaseException {
    public ProductNameRequiredException(ErrorCode errorCode) {
        super(errorCode);
    }
}
