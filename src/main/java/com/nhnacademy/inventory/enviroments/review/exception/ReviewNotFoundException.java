package com.nhnacademy.inventory.enviroments.review.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.error.InventoryErrorCode;

public class ReviewNotFoundException extends BaseException {
    public ReviewNotFoundException() {
        super(InventoryErrorCode.ENVIRONMENT_REVIEW_NOT_FOUND);
    }
}
