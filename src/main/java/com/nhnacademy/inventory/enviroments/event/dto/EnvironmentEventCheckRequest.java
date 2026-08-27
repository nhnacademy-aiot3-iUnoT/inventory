package com.nhnacademy.inventory.enviroments.event.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EnvironmentEventCheckRequest(
        @NotNull(message = "구역아이디는 필수 입력 사항입니다.")
        Long zoneId,

        @NotNull(message = "마지막검토일은 필수 입력 사항입니다.")
        LocalDateTime lastReviewAt
) {
}
