package com.nhnacademy.inventory.organizations.storage.dto;

import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import jakarta.validation.constraints.NotNull;

public record StorageStatusUpdateRequest(
        @NotNull
        StorageStatus status
) {
}
