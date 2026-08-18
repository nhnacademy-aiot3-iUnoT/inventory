package com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MedicineDisposalRequest(
        @NotNull(message = "폐기할 재고를 선택해주세요.")
        Long inventoryId,

        @NotNull(message = "폐기 수량을 입력해주세요.")
        @Positive(message = "폐기 수량은 1개 이상이어야 합니다.")
        Integer quantity,

        @NotBlank(message = "폐기 사유를 입력해주세요.")
        @Size(max = 100, message = "폐기 사유는 100자 이내로 입력해주세요.")
        String reason,

        @Size(max = 100, message = "비고는 100자 이내로 입력해주세요.")
        String memo
) {
}
