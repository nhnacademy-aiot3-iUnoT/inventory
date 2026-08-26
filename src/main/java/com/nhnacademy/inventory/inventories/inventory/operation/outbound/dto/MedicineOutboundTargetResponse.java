package com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;

public record MedicineOutboundTargetResponse(
        Long inventoryId,
        Long medicinePackageUnitId,
        String itemCode,
        String productName,
        String packUnit,
        Integer availableQuantity,
        Long storageId,
        String storageName,
        Long zoneId,
        String zoneName
) {

    public static MedicineOutboundTargetResponse from(
            MedicineInventory inventory,
            Integer availableQuantity
    ) {
        return new MedicineOutboundTargetResponse(
                inventory.getId(),
                inventory.getMedicinePackageUnit().getId(),
                inventory.getMedicinePackageUnit()
                        .getMedicine()
                        .getItemCode(),
                inventory.getMedicinePackageUnit()
                        .getMedicine()
                        .getProductName(),
                inventory.getMedicinePackageUnit()
                        .getPackUnit(),
                availableQuantity,
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
