package com.nhnacademy.inventory.medicines.enviroment.dto;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;

import java.util.List;

public record MedicineEnvironmentStandardResponse(

        Long standardId,
        Long medicinePackageUnitId,
        List<MedicineEnvironmentTypeResponse> medicineEnvironmentTypeResponses


) {
}
