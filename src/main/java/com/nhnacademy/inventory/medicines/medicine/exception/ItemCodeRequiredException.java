package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class ItemCodeRequiredException extends BaseException {
    public ItemCodeRequiredException() {
        super(MedicineErrorCode.ITEM_CODE_REQUIRED);
    }
}
