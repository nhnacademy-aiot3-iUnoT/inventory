package com.nhnacademy.inventory.medicines.medicine.dto;

import com.querydsl.core.annotations.QueryProjection;

public record MedicinePackageSearchResponse(

        Long medicineId,
        Long packageUnitId,
        String itemCode,
        String productName,
        String companyName,
        String packUnit

) {

    @QueryProjection
    public MedicinePackageSearchResponse{

    }



}
