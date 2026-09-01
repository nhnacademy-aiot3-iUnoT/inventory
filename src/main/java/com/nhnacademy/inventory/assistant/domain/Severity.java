package com.nhnacademy.inventory.assistant.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Severity {
    INFO(1),
    WARN(2),
    CRITICAL(3);

    private final int weight;
}
