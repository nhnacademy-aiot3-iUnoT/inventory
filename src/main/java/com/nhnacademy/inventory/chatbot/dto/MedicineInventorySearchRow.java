package com.nhnacademy.inventory.chatbot.dto;

import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;

public record MedicineInventorySearchRow(
        Long packageUnitId,
        String productName,
        String packUnit,
        String storageName,
        String zoneName,
        Integer currentQuantity,
        Long inventoryId,
        String lotNumber,
        LocalDate expirationDate,
        ManagementStatus managementStatus
) {
    @QueryProjection
    public MedicineInventorySearchRow {
    }
}