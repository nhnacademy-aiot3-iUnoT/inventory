package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import jakarta.validation.constraints.NotNull;

public record ZoneEnvStatusUpdateRequest(
        @NotNull(message = "환경 상태를 입력해야 합니다.")
        EnvStatus envStatus
) {
}
