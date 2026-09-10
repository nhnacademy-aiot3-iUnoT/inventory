package com.nhnacademy.inventory.enviroments.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BreachType {
    ABOVE_MAX("최대값 초과"),
    BELOW_MIN("최소값 미만");

    private final String ko;
}
