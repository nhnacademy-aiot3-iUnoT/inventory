package com.nhnacademy.inventory.inventories.inventory.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class DisposalNotFoundException extends BaseException {
    public DisposalNotFoundException() {
        super(InventoryErrorCode.DISPOSAL_NOT_FOUND);
    }
}
