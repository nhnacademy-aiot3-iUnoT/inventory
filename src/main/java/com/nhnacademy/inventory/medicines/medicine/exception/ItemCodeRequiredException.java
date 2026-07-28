package com.nhnacademy.inventory.medicines.medicine.exception;

public class ItemCodeRequiredException extends RuntimeException {
    public ItemCodeRequiredException(String message) {
        super(message);
    }
}
