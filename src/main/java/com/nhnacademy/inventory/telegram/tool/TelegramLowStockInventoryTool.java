package com.nhnacademy.inventory.telegram.tool;

import com.nhnacademy.inventory.chatbot.dto.LowStockInventoryRow;
import com.nhnacademy.inventory.telegram.service.TelegramInventoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramLowStockInventoryTool {

    private final TelegramInventoryQueryService inventoryQueryService;

    @Tool(
            name = "getLowStockInventory",
            description = """
                최소재고 기준 이하이거나 품절인 의약품을 조회합니다.

                사용 예시
                - 재고 부족한 약 뭐 있어?
                - 품절된 약 알려줘
            """)
    public List<LowStockInventoryRow> getLowStockInventory(
            @ToolParam(description = "저장소명", required = false) String storageName,
            @ToolParam(description = "구역명", required = false) String zoneName,
            ToolContext toolContext
    ) {
        return inventoryQueryService.findLowStockInventory(
                TelegramToolContext.storageIds(toolContext),
                storageName,
                zoneName
        );
    }
}
