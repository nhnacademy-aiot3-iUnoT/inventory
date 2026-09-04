package com.nhnacademy.inventory.inventories.inventory.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class InvalidExpiredDisposalTargetException extends BaseException {

    public InvalidExpiredDisposalTargetException() {
        super(InventoryErrorCode.INVALID_EXPIRED_DISPOSAL_TARGET);
    }
}