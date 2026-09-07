package com.nhnacademy.inventory.telegram.tool;

import com.nhnacademy.inventory.chatbot.dto.MedicineInventorySearchRow;
import com.nhnacademy.inventory.telegram.service.TelegramInventoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 조회 범위는 ToolContext로 받는다. LLM에는 노출되지 않으므로 모델이 범위를 바꿀 수 없다.
 */
@Component
@RequiredArgsConstructor
public class TelegramMedicineInventorySearchTool {

    private final TelegramInventoryQueryService inventoryQueryService;

    @Tool(
            name = "searchMedicineInventory",
            description = """
                의약품의 재고와 위치를 조회합니다.

                사용 예시
                - 타이레놀 몇 개 남았어?
                - 타이레놀 어디에 있어?

                의약품명은 필수입니다.
                저장소명과 구역명은 선택사항입니다.
            """)
    public List<MedicineInventorySearchRow> searchMedicineInventory(
            @ToolParam(description = "의약품명") String keyword,
            @ToolParam(description = "저장소명", required = false) String storageName,
            @ToolParam(description = "구역명", required = false) String zoneName,
            ToolContext toolContext
    ) {
        return inventoryQueryService.searchMedicineInventory(
                TelegramToolContext.storageIds(toolContext),
                keyword,
                storageName,
                zoneName
        );
    }
}
