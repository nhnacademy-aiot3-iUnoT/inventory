package com.nhnacademy.inventory.assistant.rule;

import com.nhnacademy.inventory.assistant.domain.TargetType;

public record TargetReference(
        TargetType type,
        Long id
) {}
