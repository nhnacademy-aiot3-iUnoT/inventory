package com.nhnacademy.inventory.chatbot.dto;

import com.querydsl.core.annotations.QueryProjection;

public record MedicinePackageUnitTargetRow(
        Long packageUnitId,
        String medicineName,
        String packUnit
) {
    @QueryProjection
    public MedicinePackageUnitTargetRow {
    }
}
