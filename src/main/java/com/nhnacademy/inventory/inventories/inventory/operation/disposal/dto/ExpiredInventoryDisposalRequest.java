package com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.List;

public record ExpiredInventoryDisposalRequest(
        @NotEmpty(message = "폐기할 재고를 한 개 이상 선택해주세요.")
        @Size(max = 100, message = "한 번에 최대 100건까지 폐기할 수 있습니다.")
        List<@NotNull(message = "재고 ID는 필수입니다.") Long> inventoryIds
) {

    @AssertTrue(message = "동일한 재고가 중복 선택되었습니다.")
    public boolean isInventoryIdsUnique() {
        return inventoryIds == null
                || new HashSet<>(inventoryIds).size() == inventoryIds.size();
    }
}
