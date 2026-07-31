package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class MedicineNotFoundException extends BaseException {
    public MedicineNotFoundException(MedicineErrorCode errorCode) {
        super(errorCode);
    }
}
