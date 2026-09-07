package com.nhnacademy.inventory.chatbot.dto.query;

import java.util.List;

public record FindZoneTargetQuery(
        List<Long> storageIds,
        String storageName,
        String zoneName,
        int limit
) {
}
