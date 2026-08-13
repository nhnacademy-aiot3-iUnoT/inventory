package com.nhnacademy.inventory.organizations.sensor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ZoneSensorCreateRequest(
        @NotBlank(message = "device Eui는 필수 입력 사항 입니다.")
        @Size(max = 50, message = "device Eui는 최대 50자 입니다.")
        String deviceEui,

        @NotBlank(message = "이름은 필수 입력 사항 입니다.")
        @Size(max = 50, message = "이름은 최대 50자 입니다.")
        String name,

        @Size(max = 255, message = "설명은 최대 255자 입니다.")
        String description
) {
}
