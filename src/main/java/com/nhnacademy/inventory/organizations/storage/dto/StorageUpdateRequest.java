package com.nhnacademy.inventory.organizations.storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StorageUpdateRequest(
        @NotBlank @Size(max = 50)
        String name,

        @Size(max = 255)
        String description
) {
}
