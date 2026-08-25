package com.nhnacademy.inventory.enviroments.event.dto;

import com.nhnacademy.inventory.enviroments.event.domain.BreachType;
import com.nhnacademy.inventory.enviroments.event.domain.EnvironmentEvent;
import com.nhnacademy.inventory.enviroments.event.domain.EnvironmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnvironmentEventItemResponse(
        Long environmentEventId,
        BigDecimal detectedValue,
        BigDecimal thresholdValue,
        EnvironmentType environmentType,
        BreachType breachType,
        LocalDateTime createdAt
) {
    public static EnvironmentEventItemResponse from(EnvironmentEvent event){
        return new EnvironmentEventItemResponse(
                event.getId(),
                event.getDetectedValue(),
                event.getThresholdValue(),
                event.getEnvironmentType(),
                event.getBreachType(),
                event.getCreatedAt()
        );
    }
}
