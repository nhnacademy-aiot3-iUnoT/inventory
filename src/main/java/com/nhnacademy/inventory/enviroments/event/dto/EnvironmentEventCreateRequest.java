package com.nhnacademy.inventory.enviroments.event.dto;

import com.nhnacademy.inventory.enviroments.event.domain.BreachType;
import com.nhnacademy.inventory.enviroments.event.domain.EnvironmentType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EnvironmentEventCreateRequest(

        @NotNull
        Long zoneId,

        @NotNull
        BigDecimal detectedValue,

        @NotNull
        BigDecimal thresholdValue,

        @NotNull
        EnvironmentType environmentType,

        @NotNull
        BreachType breachType
) {
}
