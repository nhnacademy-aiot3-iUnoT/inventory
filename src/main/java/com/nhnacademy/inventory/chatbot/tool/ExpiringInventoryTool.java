package com.nhnacademy.inventory.chatbot.tool;

import com.nhnacademy.inventory.chatbot.dto.response.ExpiringInventoryResponse;
import com.nhnacademy.inventory.chatbot.service.MedicineInventoryChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpiringInventoryTool {
    private final MedicineInventoryChatbotService medicineInventoryChatbotService;

    @Tool(
            name = "getExpiringInventory",
            description = """
            지정한 일수 이내에 유통기한이 만료되는 재고를 조회합니다.

            사용 예시
            - 30일 이내 만료되는 의약품 알려줘
            - 이번 달에 유통기한이 끝나는 재고 보여줘

            조회 일수는 선택사항이며, 입력하지 않으면 30일 기준으로 조회합니다.
            조회 일수는 1일에서 90일 사이여야 합니다.
            """
    )
    public ExpiringInventoryResponse getExpiringInventory(
            @ToolParam(description = "유통기한까지 남은 일수 (1~90일, 미입력 시 30일)", required = false) Integer daysRemaining,
            @ToolParam(description = "의약품명", required = false) String medicineName,
            @ToolParam(description = "저장소명", required = false) String storageName,
            @ToolParam(description = "구역명", required = false) String zoneName
    ) {
        return medicineInventoryChatbotService.getExpiringInventory(daysRemaining, medicineName, storageName, zoneName);
    }
}
