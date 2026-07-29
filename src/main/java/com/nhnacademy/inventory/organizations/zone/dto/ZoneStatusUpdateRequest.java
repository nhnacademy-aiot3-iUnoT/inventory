package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import jakarta.validation.constraints.NotNull;

public record ZoneStatusUpdateRequest(
        @NotNull
        ZoneStatus status
) {
}
