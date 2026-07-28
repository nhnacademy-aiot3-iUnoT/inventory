package com.nhnacademy.inventory.medicines.medicine.exception;

public class ProductNameRequiredException extends RuntimeException {
    public ProductNameRequiredException(String message) {
        super(message);
    }
}
