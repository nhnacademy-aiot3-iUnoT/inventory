package com.nhnacademy.inventory.medicines.enviroment.dto;

import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record MedicineEnvironmentTypeRequest(

        @NotNull(message = "환경 유형을 선택해주세요")
        EnvironmentType environmentType,
        @NotNull(message = "최소 기준값을 입력해주세요.")
        BigDecimal min,
        @NotNull(message = "최대 기준값을 입력해주세요.")
        BigDecimal max


) {
}
