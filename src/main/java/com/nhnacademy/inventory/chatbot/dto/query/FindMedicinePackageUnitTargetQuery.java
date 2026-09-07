package com.nhnacademy.inventory.chatbot.dto.query;

public record FindMedicinePackageUnitTargetQuery(
        String medicineName,
        String packUnit,
        int limit
) {
}
