package com.nhnacademy.inventory.organizations.zone.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ThresholdSaveRequest(
        @NotNull
        Long sensorTypeId,

        @Digits(integer = 8, fraction = 2)
        BigDecimal minValue,

        @Digits(integer = 8, fraction = 2)
        BigDecimal maxValue,

        @NotNull @Positive
        Integer alertDuration
){
}
