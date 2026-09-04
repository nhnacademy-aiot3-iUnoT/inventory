package com.nhnacademy.inventory.chatbot.tool;

import com.nhnacademy.inventory.chatbot.dto.request.InboundToolRequest;
import com.nhnacademy.inventory.chatbot.dto.response.InboundToolResponse;
import com.nhnacademy.inventory.chatbot.service.ChatbotInventoryOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicineInboundTool {
    private final ChatbotInventoryOperationService operationService;

    @Tool(
            name = "registerMedicineInbound",
            description = """
                사용자가 최종 확인한 의약품 입고를 처리합니다.
                실제 재고 수량이 변경되므로 의약품명, 포장단위, 저장소명, 구역명,
                제조번호, 유통기한, 수량을 모두 확인한 후에만 호출해야 합니다.
            """
    )
    public InboundToolResponse inbound(
            @ToolParam(description = "입고 처리 정보") InboundToolRequest request
    ) {
        if (request == null) {
            log.info("event=chatbot_inbound_tool_called requestPresent=false");
        } else {
            log.info(
                    "event=chatbot_inbound_tool_called medicinePackageUnitId={} medicineName='{}' "
                            + "packUnit='{}' zoneId={} storageName='{}' zoneName='{}' quantity={}",
                    request.medicinePackageUnitId(),
                    request.medicineName(),
                    request.packUnit(),
                    request.zoneId(),
                    request.storageName(),
                    request.zoneName(),
                    request.quantity()
            );
        }

        return operationService.inbound(request);
    }
}
