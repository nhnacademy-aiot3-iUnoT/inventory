package com.nhnacademy.inventory.enviroments.event.dto;

import com.nhnacademy.inventory.enviroments.event.domain.BreachType;
import com.nhnacademy.inventory.enviroments.event.domain.EnvironmentType;
import com.querydsl.core.annotations.QueryProjection;

import java.math.BigDecimal;

public record EnvironmentEventInfoResponse(
        Long environmentEventId,
        Long OrganizationId,
        Long StorageId,
        Long zoneId,
        String OrganizationName,
        String StorageName,
        String ZoneName,
        BigDecimal detectedValue,
        BigDecimal thresholdValue,
        EnvironmentType environmentType,
        BreachType breachType
) {
    @QueryProjection
    public EnvironmentEventInfoResponse{

    }
}
