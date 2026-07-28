package com.nhnacademy.inventory.medicines.medicine.exception;

public class CompanyNameRequiredException extends RuntimeException {
    public CompanyNameRequiredException(String message) {
        super(message);
    }
}
