package com.nhnacademy.inventory.inventories.threshold.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record StockThresholdSaveRequest(
        @NotNull(message = "단위의약품 ID를 입력해야 합니다.")
        Long medicinePackageUnitId,

        @NotNull(message = "최소재고를 입력해야 합니다.")
        @Positive(message = "최소재고는 음수일 수 없습니다.")
        Integer stockThreshold
) {
}
