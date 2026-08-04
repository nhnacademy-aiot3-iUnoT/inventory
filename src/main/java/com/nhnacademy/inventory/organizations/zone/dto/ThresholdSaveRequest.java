package com.nhnacademy.inventory.organizations.zone.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ThresholdSaveRequest(
        @NotNull(message = "센서타입 ID를 입력해야 합니다.")
        Long sensorTypeId,

        @Digits(integer = 8, fraction = 2, message = "소수부는 최대 두자리입니다.")
        BigDecimal minValue,

        @Digits(integer = 8, fraction = 2, message = "소수부는 최대 두자리입니다.")
        BigDecimal maxValue,

        @NotNull(message = "지속시간을 입력해야 합니다.")
        @Positive(message = "지속시간은 음수일 수 없습니다.")
        Integer alertDuration
){
}
