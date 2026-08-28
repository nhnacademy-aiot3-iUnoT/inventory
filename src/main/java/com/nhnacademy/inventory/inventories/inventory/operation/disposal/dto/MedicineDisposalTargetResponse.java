package com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;

import java.time.LocalDate;

public record MedicineDisposalTargetResponse(
        Long inventoryId,
        String itemCode,
        String productName,
        String packUnit,
        String lotNumber,
        LocalDate expirationDate,
        Integer currentQuantity,
        Long storageId,
        String storageName,
        Long zoneId,
        String zoneName
) {

    public static MedicineDisposalTargetResponse from(
            MedicineInventory inventory
    ) {
        return new MedicineDisposalTargetResponse(
                inventory.getId(),
                inventory.getMedicinePackageUnit()
                        .getMedicine()
                        .getItemCode(),
                inventory.getMedicinePackageUnit()
                        .getMedicine()
                        .getProductName(),
                inventory.getMedicinePackageUnit()
                        .getPackUnit(),
                inventory.getLotNumber(),
                inventory.getExpirationDate(),
                inventory.getCurrentQuantity(),
                inventory.getZone()
                        .getStorage()
                        .getId(),
                inventory.getZone()
                        .getStorage()
                        .getName(),
                inventory.getZone().getId(),
                inventory.getZone().getName()
        );
    }
}