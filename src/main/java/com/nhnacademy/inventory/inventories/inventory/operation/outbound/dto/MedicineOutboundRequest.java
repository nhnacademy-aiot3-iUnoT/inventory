package com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto;

import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

public record MedicineOutboundRequest(

        @NotNull(message = "의약품을 선택해주세요.")
        Long medicinePackageUnitId,

        @NotNull(message = "수량을 입력해주세요.")
        @Positive(message = "수량은 1개 이상이어야 합니다.")
        Integer quantity,

        @NotNull(message = "구역을 선택해주세요.")
        Long zoneId,

        @NotNull(message = "출고 사유를 선택해주세요.")
        OutboundReason reason,

        @Size(max = 100, message = "비고는 100자 이내로 입력해주세요.")
        String memo

) {
        @AssertTrue(
                message = "저장소 이동 또는 기타 사유는 상세 내용을 입력해주세요."
        )
        public boolean isMemoValid() {
                if (reason == null) {
                        return true;
                }
                boolean memoRequired =
                        reason == OutboundReason.STORAGE_TRANSFER
                                || reason == OutboundReason.OTHER;

                if (!memoRequired) {
                        return true;
                }
                return memo != null && !memo.isBlank();
        }
}
