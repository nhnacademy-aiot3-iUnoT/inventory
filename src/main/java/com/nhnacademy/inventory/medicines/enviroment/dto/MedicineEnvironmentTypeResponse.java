package com.nhnacademy.inventory.medicines.enviroment.dto;

import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;

import java.math.BigDecimal;

public record MedicineEnvironmentTypeResponse(

        String type,
        BigDecimal min,
        BigDecimal max


) {

    public static MedicineEnvironmentTypeResponse from(MedicineEnvironmentType type){

        return new MedicineEnvironmentTypeResponse(
                type.getEnvironmentType().getKo(),
                type.getMin(),
                type.getMax()

        );

    }


}
