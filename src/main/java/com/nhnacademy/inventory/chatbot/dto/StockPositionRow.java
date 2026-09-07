package com.nhnacademy.inventory.chatbot.dto;

import com.querydsl.core.annotations.QueryProjection;

public record StockPositionRow(
        Long packageUnitId,
        Long storageId,
        String productName,
        String packUnit,
        String storageName,
        Long currentQuantity,
        Long expiringSoonQuantity,
        Integer threshold
) {

    @QueryProjection
    public StockPositionRow{

    }
}
