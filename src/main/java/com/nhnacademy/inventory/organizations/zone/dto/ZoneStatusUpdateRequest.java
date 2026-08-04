package com.nhnacademy.inventory.organizations.zone.dto;

import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import jakarta.validation.constraints.NotNull;

public record ZoneStatusUpdateRequest(
        @NotNull(message = "구역 상태를 입력해야 합니다.")
        ZoneStatus status
) {
}
