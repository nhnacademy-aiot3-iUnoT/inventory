package com.nhnacademy.inventory.organizations.zone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SensorTypeCreateRequest(
        @NotBlank @Size(max = 30)
        String name,

        @Size(max = 255)
        String description
) {
}
