package com.nhnacademy.inventory.organizations.zone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ZoneCreateRequest(
        @NotBlank(message = "구역 이름을 입력해야 합니다.")
        @Size(max = 30, message = "구역 이름은 최대 30자 입니다.")
        String name,

        @Size(max = 255, message = "구역 설명은 최대 255자 입니다.")
        String description
) {
}
