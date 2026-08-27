package com.nhnacademy.inventory.enviroments.event.dto;

import com.nhnacademy.inventory.enviroments.event.domain.BreachType;
import com.nhnacademy.inventory.enviroments.event.domain.EnvironmentType;

public record EnvironmentEventSearchCondition(
        EnvironmentType environmentType,
        BreachType breachType
) {
}
