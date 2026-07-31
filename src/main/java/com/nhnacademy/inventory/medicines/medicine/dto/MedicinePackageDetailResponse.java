package com.nhnacademy.inventory.medicines.medicine.dto;

public record MedicinePackageDetailResponse(

        Long medicineId,
        Long packageUnitId,
        String itemCode,
        String productName,
        String companyName,
        String storageMethod,
        String validityPeriod,
        String narcoticKindCode


) {
}
