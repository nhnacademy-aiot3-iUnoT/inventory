package com.nhnacademy.inventory.enviroments.event.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EnvironmentEventCheckRequest(
        @NotNull
        Long zoneId,

        @NotNull
        LocalDateTime lastReviewAt
) {
}
