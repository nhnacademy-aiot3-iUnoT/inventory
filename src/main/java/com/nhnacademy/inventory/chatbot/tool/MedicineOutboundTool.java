package com.nhnacademy.inventory.chatbot.tool;

import com.nhnacademy.inventory.chatbot.dto.request.OutboundToolRequest;
import com.nhnacademy.inventory.chatbot.dto.response.OutboundToolResponse;
import com.nhnacademy.inventory.chatbot.service.ChatbotInventoryOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MedicineOutboundTool {
    private final ChatbotInventoryOperationService operationService;

    @Tool(
            name = "registerMedicineOutbound",
            description = """
                사용자가 최종 확인한 의약품 출고를 처리합니다.
                출고할 로트를 지정해야 하므로 searchMedicineInventory 로 로트 목록을 조회해
                로트번호와 유통기한, 수량을 보여주고 어느 로트인지 확인받으세요.
                로트가 하나뿐이어도 확인받아야 합니다.

                사용자가 로트를 고르면 이 도구를 호출하기 직전에
                searchMedicineInventory 를 다시 호출해 그 로트의 inventoryId 를 확인하세요.
                inventoryId 는 Tool 결과에만 있어 다음 차례에는 남지 않으므로
                같은 차례에서 조회한 값만 사용하고 추측하지 마세요.
            """
    )
    public OutboundToolResponse outbound(
            @ToolParam(description = "출고 처리 정보") OutboundToolRequest request
    ) {
        return operationService.outbound(request);
    }
}
