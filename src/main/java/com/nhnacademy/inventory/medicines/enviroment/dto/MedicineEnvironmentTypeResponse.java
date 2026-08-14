package com.nhnacademy.inventory.medicines.enviroment.dto;

import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentType;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;

import java.math.BigDecimal;

public record MedicineEnvironmentTypeResponse(

        EnvironmentType type,
        BigDecimal min,
        BigDecimal max


) {

    public static MedicineEnvironmentTypeResponse from(MedicineEnvironmentType type){

        return new MedicineEnvironmentTypeResponse(
                type.getEnvironmentType(),
                type.getMin(),
                type.getMax()

        );

    }


}
