package com.nhnacademy.inventory.medicines.medicine.dto;

import com.querydsl.core.annotations.QueryProjection;

public record MedicinePackageDetailResponse(

        Long medicineId,
        Long packageUnitId,
        String itemCode,
        String productName,
        String companyName,
        String storageMethod,
        String validityPeriod,
        String packUnit,
        String narcoticKindCode


) {

    @QueryProjection
    public MedicinePackageDetailResponse {
    }
}
