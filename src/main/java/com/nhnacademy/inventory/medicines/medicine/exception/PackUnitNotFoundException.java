package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class PackUnitNotFoundException extends BaseException {
    public PackUnitNotFoundException(MedicineErrorCode errorCode) {
        super(errorCode);
    }
}
