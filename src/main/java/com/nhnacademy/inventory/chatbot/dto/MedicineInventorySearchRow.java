package com.nhnacademy.inventory.chatbot.dto;

import com.querydsl.core.annotations.QueryProjection;

public record MedicineInventorySearchRow(
        Long packageUnitId,
        String productName,
        String packUnit,
        String storageName,
        String zoneName,
        Integer currentQuantity
) {
    @QueryProjection
    public MedicineInventorySearchRow {
    }
}
