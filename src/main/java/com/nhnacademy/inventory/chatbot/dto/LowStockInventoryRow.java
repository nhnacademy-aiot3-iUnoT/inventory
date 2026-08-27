package com.nhnacademy.inventory.chatbot.dto;

import com.querydsl.core.annotations.QueryProjection;

public record LowStockInventoryRow(
        String productName,
        String packUnit,
        Integer currentQuantity,
        Integer threshold,
        String storageName,
        String zoneName
) {
    @QueryProjection
    public LowStockInventoryRow {
    }
}
