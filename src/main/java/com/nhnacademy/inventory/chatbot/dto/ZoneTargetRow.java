package com.nhnacademy.inventory.chatbot.dto;

import com.querydsl.core.annotations.QueryProjection;

public record ZoneTargetRow(
        Long zoneId,
        String storageName,
        String zoneName
) {
    @QueryProjection
    public ZoneTargetRow {
    }
}
