package com.nhnacademy.inventory.medicines.enviroment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnvironmentType {
    TEMPERATURE("온도"),
    HUMIDITY("습도"),
    ILLUMINANCE("조도");

    private final String ko;
}
