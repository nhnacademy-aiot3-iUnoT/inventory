package com.nhnacademy.inventory.chatbot.dto.request;

import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDate;

public record InboundToolRequest(
        @ToolParam(
                description = "후보 검색 결과에서 선택한 의약품 포장단위 ID. ID가 있으면 의약품명과 포장단위보다 우선합니다.",
                required = false
        )
        @Positive(message = "의약품 포장단위 ID는 1 이상이어야 합니다.")
        Long medicinePackageUnitId,

        @ToolParam(description = "입고할 의약품명")
        @NotBlank(message = "의약품명은 필수입니다.")
        String medicineName,

        @ToolParam(description = "의약품 포장단위")
        @NotBlank(message = "포장단위는 필수입니다.")
        String packUnit,

        @ToolParam(
                description = "후보 검색 결과에서 선택한 구역 ID. ID가 있으면 저장소명과 구역명보다 우선합니다.",
                required = false
        )
        @Positive(message = "구역 ID는 1 이상이어야 합니다.")
        Long zoneId,

        @ToolParam(description = "입고할 저장소명")
        @NotBlank(message = "저장소명은 필수입니다.")
        String storageName,

        @ToolParam(description = "입고할 구역명")
        @NotBlank(message = "구역명은 필수입니다.")
        String zoneName,

        @ToolParam(description = "제조번호")
        @NotBlank(message = "제조번호는 필수입니다.")
        @Size(min = 5, max = 50, message = "제조번호는 5자 이상 50자 이하여야 합니다.")
        String lotNumber,

        @ToolParam(description = "유통기한, yyyy-MM-dd 형식")
        @NotNull(message = "유통기한은 필수입니다.")
        @FutureOrPresent(message = "유통기한은 오늘 이후여야 합니다.")
        LocalDate expirationDate,

        @ToolParam(description = "입고 수량")
        @NotNull(message = "입고 수량은 필수입니다.")
        @Positive(message = "입고 수량은 1개 이상이어야 합니다.")
        Integer quantity,

        @ToolParam(description = "변동 유형")
        @NotNull(message = "입고 유형은 필수입니다.")
        TransactionType transactionType,

        @ToolParam(description = "입고 메모", required = false)
        @Size(max = 255, message = "메모는 255자 이하여야 합니다.")
        String memo
) {
}
