package com.nhnacademy.inventory.inventories.threshold.dto;

import com.nhnacademy.inventory.inventories.threshold.domain.StockThreshold;

public record StockThresholdInfoResponse(
        Long stockThresholdId,
        Long medicinePackageUnitId,
        Long storageId,
        Integer stockThreshold,
        Boolean isActive
) {
    public static StockThresholdInfoResponse from(StockThreshold stockThreshold){
        return new StockThresholdInfoResponse(
                stockThreshold.getId(),
                stockThreshold.getMedicinePackageUnit().getId(),
                stockThreshold.getStorage().getId(),
                stockThreshold.getThreshold(),
                stockThreshold.getIsActive()
        );
    }
}
