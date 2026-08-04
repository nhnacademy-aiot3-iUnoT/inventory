package com.nhnacademy.inventory.medicines.medicine.exception;


import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.global.error.ErrorCode;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;

public class CompanyNameRequiredException extends BaseException {
    public CompanyNameRequiredException() {
        super(MedicineErrorCode.COMPANY_NAME_REQUIRED);
    }
}
