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
                실제 재고 수량이 변경되므로 의약품명, 포장단위, 저장소명, 구역명,
                수량과 출고 사유를 모두 확인한 후에만 호출해야 합니다.
            """
    )
    public OutboundToolResponse outbound(
            @ToolParam(description = "출고 처리 정보") OutboundToolRequest request
    ) {
        return operationService.outbound(request);
    }
}
