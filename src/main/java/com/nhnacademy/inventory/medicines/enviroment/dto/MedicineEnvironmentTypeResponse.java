package com.nhnacademy.inventory.medicines.enviroment.dto;

import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentType;

import java.math.BigDecimal;

public record MedicineEnvironmentTypeResponse(

        EnvironmentType environmentType,
        BigDecimal min,
        BigDecimal max


) {
}
