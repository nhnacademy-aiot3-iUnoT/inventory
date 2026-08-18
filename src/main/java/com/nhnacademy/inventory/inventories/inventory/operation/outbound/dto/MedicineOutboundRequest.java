package com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto;

import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MedicineOutboundRequest(

        @NotNull(message = "의약품을 선택해주세요.")
        Long medicinePackageUnitId,

        @NotNull(message = "수량을 입력해주세요.")
        @Positive(message = "수량은 1개 이상이어야 합니다.")
        Integer quantity,

        @NotNull(message = "구역을 선택해주세요.")
        Long zoneId,

        @NotNull(message = "출고 사유를 선택해주세요.")
        TransactionType transactionType,

        @Size(max = 100, message = "비고는 100자 이내로 입력해주세요.")
        String memo

) {


}
