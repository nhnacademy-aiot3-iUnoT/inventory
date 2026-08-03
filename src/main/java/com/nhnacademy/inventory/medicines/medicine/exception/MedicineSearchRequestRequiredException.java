package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class MedicineSearchRequestRequiredException extends BaseException {
    public MedicineSearchRequestRequiredException() {
        super(MedicineErrorCode.MEDICINE_SEARCH_REQUEST_REQUIRED);
    }
}
