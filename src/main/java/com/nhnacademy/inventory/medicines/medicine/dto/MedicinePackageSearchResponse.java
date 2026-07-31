package com.nhnacademy.inventory.medicines.medicine.dto;

public record MedicinePackageSearchResponse(

        Long medicineId,
        Long packageUnitId,
        String itemCode,
        String productName,
        String companyName,
        String packUnit

) {
}
