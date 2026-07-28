package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import jakarta.validation.constraints.NotNull;

public record ZoneEnvStatusUpdateRequest(
        @NotNull
        EnvStatus envStatus
) {
}
