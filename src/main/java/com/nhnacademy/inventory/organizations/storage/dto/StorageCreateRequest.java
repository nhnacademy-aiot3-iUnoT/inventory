package com.nhnacademy.inventory.organizations.storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StorageCreateRequest(
        @NotBlank @Size(max = 50)
        String name,

        @Size(max = 255)
        String description,

        List<Long> departmentIds
) {
}
