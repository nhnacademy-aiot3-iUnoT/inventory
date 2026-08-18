package com.nhnacademy.inventory.inventories.inventory.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class InvalidOutboundTypeException extends BaseException {

    public InvalidOutboundTypeException() {
        super(InventoryErrorCode.INVALID_OUTBOUND_TYPE);
    }
}
