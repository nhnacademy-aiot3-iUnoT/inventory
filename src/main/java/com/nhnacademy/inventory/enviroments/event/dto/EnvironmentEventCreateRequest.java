package com.nhnacademy.inventory.enviroments.event.dto;

import com.nhnacademy.inventory.enviroments.event.domain.BreachType;
import com.nhnacademy.inventory.enviroments.event.domain.EnvironmentType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EnvironmentEventCreateRequest(

        @NotNull(message = "구역아이디는 필수입력 사항입니다.")
        Long zoneId,

        @NotNull(message = "감지된 값은 필수입력 사항입니다.")
        BigDecimal detectedValue,

        @NotNull(message = "임계값은 필수입력 사항입니다.")
        BigDecimal thresholdValue,

        @NotNull(message = "환경유형은 필수입력 사항입니다.")
        EnvironmentType environmentType,

        @NotNull(message = "위반유형은 필수입력 사항입니다.")
        BreachType breachType
) {
}
