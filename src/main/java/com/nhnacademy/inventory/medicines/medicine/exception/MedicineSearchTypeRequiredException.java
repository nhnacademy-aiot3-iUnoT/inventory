package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class MedicineSearchTypeRequiredException extends BaseException {
    public MedicineSearchTypeRequiredException() {
        super(MedicineErrorCode.MEDICINE_SEARCH_TYPE_REQUIRED);
    }
}
