package com.nhnacademy.inventory.chatbot.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;

public record ExpiringInventoryRow(
        String productName,
        String packUnit,
        String lotNumber,
        LocalDate expirationDate,
        Integer currentQuantity,
        String storageName,
        String zoneName
) {
    @QueryProjection
    public ExpiringInventoryRow {
    }
}
