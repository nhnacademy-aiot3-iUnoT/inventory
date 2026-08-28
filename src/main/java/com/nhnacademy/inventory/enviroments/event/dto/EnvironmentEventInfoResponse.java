package com.nhnacademy.inventory.enviroments.event.dto;

import com.nhnacademy.inventory.enviroments.event.domain.BreachType;
import com.nhnacademy.inventory.enviroments.event.domain.EnvironmentType;
import com.querydsl.core.annotations.QueryProjection;

import java.math.BigDecimal;

public record EnvironmentEventInfoResponse(
        Long environmentEventId,
        Long organizationId,
        Long storageId,
        Long zoneId,
        String organizationName,
        String storageName,
        String zoneName,
        BigDecimal detectedValue,
        BigDecimal thresholdValue,
        EnvironmentType environmentType,
        BreachType breachType
) {
    @QueryProjection
    public EnvironmentEventInfoResponse{

    }
}
