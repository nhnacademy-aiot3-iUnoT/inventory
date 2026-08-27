package com.nhnacademy.inventory.chatbot.tool;

import com.nhnacademy.inventory.chatbot.dto.response.LowStockInventoryResponse;
import com.nhnacademy.inventory.chatbot.service.MedicineInventoryChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LowStockInventoryTool {
    private final MedicineInventoryChatbotService medicineInventoryChatbotService;

    @Tool(
            name = "getLowStockInventory",
            description = """
                최소재고 기준 이하인 의약품을 조회합니다.
    
                사용 예시
                - 재고 부족한 의약품 알려줘
                - 발주가 필요한 재고 알려줘
    
                저장소명과 구역명은 선택사항입니다.
            """
    )
    public LowStockInventoryResponse getLowStockInventory(
            @ToolParam(description = "저장소명", required = false) String storageName,
            @ToolParam(description = "구역명", required = false) String zoneName
    ) {
        return medicineInventoryChatbotService.getLowStockInventory(storageName, zoneName);
    }
}
