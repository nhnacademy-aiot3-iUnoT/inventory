package com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto;

import com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain.DisposalReason;
import jakarta.validation.constraints.*;

public record MedicineDisposalRequest(
        @NotNull(message = "폐기 수량을 입력해주세요.")
        @Positive(message = "폐기 수량은 1개 이상이어야 합니다.")
        Integer quantity,

        @NotNull(message = "폐기 사유를 선택해주세요.")
        DisposalReason reason,

        @Size(max = 100, message = "상세 사유는 100자 이내로 입력해주세요.")
        String memo
) {
        @AssertTrue(message = "상세 사유는 기타 선택 시에만 입력해야 합니다.")
        public boolean isMemoValid() {
                if (reason == null) {
                        return true;
                }

                boolean hasMemo = memo != null && !memo.isBlank();

                if (reason == DisposalReason.OTHER) {
                        return hasMemo;
                }

                return !hasMemo;
        }
}
