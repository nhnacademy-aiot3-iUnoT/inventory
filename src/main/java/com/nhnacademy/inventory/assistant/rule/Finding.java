package com.nhnacademy.inventory.assistant.rule;

import com.nhnacademy.inventory.assistant.domain.Severity;

public record Finding(
        FindingType type,
        Severity severity,
        String describe,
        TargetReference target
) {}
