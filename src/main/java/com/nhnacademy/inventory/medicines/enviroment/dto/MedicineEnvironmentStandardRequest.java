package com.nhnacademy.inventory.medicines.enviroment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MedicineEnvironmentStandardRequest(

        @NotEmpty(message= "환경 기준을 하나 이상 입력해주세요.")
        List<@Valid MedicineEnvironmentTypeRequest> environmentTypes

) {
}
