package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;

public class MedicineRequiredException extends BaseException {
    public MedicineRequiredException(ErrorCode errorCode) {
        super(errorCode);
    }
}
