package com.nhnacademy.inventory.telegram.tool;

import com.nhnacademy.inventory.chatbot.dto.ExpiringInventoryRow;
import com.nhnacademy.inventory.telegram.service.TelegramInventoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramExpiringInventoryTool {

    private final TelegramInventoryQueryService inventoryQueryService;

    @Tool(
            name = "getExpiringInventory",
            description = """
                유통기한이 임박한 의약품을 조회합니다.

                사용 예시
                - 유통기한 얼마 안 남은 약 있어?
                - 7일 안에 만료되는 약 알려줘

                조회 일수는 선택사항이며 기본 30일, 최대 90일입니다.
            """)
    public List<ExpiringInventoryRow> getExpiringInventory(
            @ToolParam(description = "조회 일수", required = false) Integer searchDays,
            @ToolParam(description = "의약품명", required = false) String medicineName,
            @ToolParam(description = "저장소명", required = false) String storageName,
            @ToolParam(description = "구역명", required = false) String zoneName,
            ToolContext toolContext
    ) {
        return inventoryQueryService.findExpiringInventory(
                TelegramToolContext.storageIds(toolContext),
                searchDays,
                medicineName,
                storageName,
                zoneName
        );
    }
}
