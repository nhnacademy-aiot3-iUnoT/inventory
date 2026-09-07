package com.nhnacademy.inventory.telegram.tool;

import org.springframework.ai.chat.model.ToolContext;

import java.util.List;
import java.util.Map;


public final class TelegramToolContext {

    private static final String STORAGE_IDS = "storageIds";

    private TelegramToolContext() {
    }

    public static Map<String, Object> of(List<Long> storageIds) {
        return Map.of(STORAGE_IDS, storageIds);
    }

    @SuppressWarnings("unchecked")
    public static List<Long> storageIds(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            throw new IllegalStateException("단톡방 챗봇 도구에 조회 범위가 전달되지 않았습니다.");
        }

        Object value = toolContext.getContext().get(STORAGE_IDS);
        if (!(value instanceof List<?> storageIds) || storageIds.isEmpty()) {
            throw new IllegalStateException("단톡방 챗봇 도구에 조회 범위가 전달되지 않았습니다.");
        }

        return (List<Long>) storageIds;
    }
}
