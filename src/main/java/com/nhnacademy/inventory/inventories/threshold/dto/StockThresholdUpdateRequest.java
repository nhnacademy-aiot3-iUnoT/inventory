package com.nhnacademy.inventory.inventories.threshold.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockThresholdUpdateRequest(
        @NotNull(message = "최소재고를 입력해야 합니다.")
        @Positive(message = "최소재고는 음수일 수 없습니다.")
        Integer stockThreshold,

        @NotNull(message = "활성화 상태값은 필수입니다.")
        Boolean isActive
) {
}
