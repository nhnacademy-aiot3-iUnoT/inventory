package com.nhnacademy.inventory.medicines.medicine.dto;

import jakarta.validation.constraints.NotNull;

public record MedicineDetailRequest(

        @NotNull(message = "해당 의약품을 선택해주세요.")
        Long packUnitId


) {
}
