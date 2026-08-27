package com.nhnacademy.inventory.chatbot.tool;

import com.nhnacademy.inventory.chatbot.dto.response.SearchMedicineInventoryResponse;
import com.nhnacademy.inventory.chatbot.service.MedicineInventoryChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MedicineInventorySearchTool {
    private final MedicineInventoryChatbotService medicineInventoryChatbotService;

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
    public SearchMedicineInventoryResponse searchMedicineInventory(
            @ToolParam(description = "의약품명") String keyword,
            @ToolParam(description = "저장소명", required = false) String storageName,
            @ToolParam(description = "구역명", required = false) String zoneName
    ) {
        return medicineInventoryChatbotService.searchMedicineInventory(keyword, storageName, zoneName);
    }
}
