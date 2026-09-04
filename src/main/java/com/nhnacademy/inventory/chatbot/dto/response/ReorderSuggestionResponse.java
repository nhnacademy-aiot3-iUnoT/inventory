package com.nhnacademy.inventory.chatbot.dto.response;

import java.util.List;

public record ReorderSuggestionResponse(
        int analyzedWeeks,  // 분석에 사용되눈 주범위
        List<Item> items
) {
    public record Item(
            String productName,
            String packUnit,
            String storageName,
            int suggestedQuantity,
            int currentQuantity,
            int expiringSoonQuantity,
            double weeklyAverageOutbound,
            int lastWeekOutbound,
            int disposalQuantity,
            double disposalRate,
            boolean adjustmentApplied,
            Integer threshold,          // null = 미설정
            Integer daysUntilStockout   // null = 산출 불가
    ) {
    }

    public ReorderSuggestionResponse(List<Item> items,int analyzedWeeks) {
        this(analyzedWeeks, items);
    }

    public static ReorderSuggestionResponse empty(int analyzedWeeks) {
        return new ReorderSuggestionResponse(analyzedWeeks, List.of());
    }
}