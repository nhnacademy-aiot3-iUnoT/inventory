package com.nhnacademy.inventory.chatbot.dto.request;

import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundReason;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.ai.tool.annotation.ToolParam;

public record OutboundToolRequest(
        @ToolParam(description = """
                사용자가 선택하고 최종 확인한 LOT의 재고 ID.
                medicinePackageUnitId 또는 zoneId가 아닌 inventoryId이며,
                조회 결과에서 확인한 값만 사용하고 추측하지 않습니다.
                """)
        @NotNull(message = "출고할 LOT의 재고 ID는 필수입니다.")
        @Positive(message = "재고 ID는 양수여야 합니다.")
        Long inventoryId,

        @ToolParam(description = "선택한 LOT에서 출고할 수량")
        @NotNull(message = "출고 수량은 필수입니다.")
        @Positive(message = "출고 수량은 1개 이상이어야 합니다.")
        Integer quantity,

        @ToolParam(description = "출고 사유: DISPENSING, STORAGE_TRANSFER, RETURN_TO_SUPPLIER, OTHER 중 하나")
        @NotNull(message = "출고 사유는 필수입니다.")
        OutboundReason reason,

        @ToolParam(description = "출고 메모. STORAGE_TRANSFER 또는 OTHER이면 필수", required = false)
        @Size(max = 100, message = "메모는 100자 이하여야 합니다.")
        String memo
) {
    @AssertTrue(message = "저장소 이동 또는 기타 사유는 메모가 필요합니다.")
    public boolean isMemoValid() {
        if (reason != OutboundReason.STORAGE_TRANSFER
                && reason != OutboundReason.OTHER) {
            return true;
        }
        return memo != null && !memo.isBlank();
    }
}