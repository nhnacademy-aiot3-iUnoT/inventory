package com.nhnacademy.inventory.medicines.medicine.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class ProductNameRequiredException extends BaseException {
    public ProductNameRequiredException() {
        super(MedicineErrorCode.PRODUCT_NAME_REQUIRED);
    }
}
