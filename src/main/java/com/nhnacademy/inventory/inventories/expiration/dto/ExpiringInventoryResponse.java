package com.nhnacademy.inventory.inventories.expiration.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;

public record ExpiringInventoryResponse(
        Long inventoryId,
        Long medicineId,
        Long medicinePackageUnitId,
        Long organizationId,
        Long storageId,
        Long zoneId,
        String medicineName,
        String packUnitName,
        String organizationName,
        String storageName,
        String zoneName,
        String lotNumber,
        LocalDate expirationDate,
        Integer currentQuantity
) {
    @QueryProjection
    public ExpiringInventoryResponse{

    }
}
