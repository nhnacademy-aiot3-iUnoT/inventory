package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class MedicineRequiredException extends BaseException {
    public MedicineRequiredException() {
        super(MedicineErrorCode.MEDICINE_REQUIRED);
    }
}
