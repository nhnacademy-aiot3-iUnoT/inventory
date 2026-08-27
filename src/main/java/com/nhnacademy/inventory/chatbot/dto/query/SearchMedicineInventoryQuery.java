package com.nhnacademy.inventory.chatbot.dto.query;

import java.util.List;

public record SearchMedicineInventoryQuery(
        List<Long> storageIds, // 권한 있는 저장소
        String keyword,
        String storageName,
        String zoneName,
        int limit
) {
}
