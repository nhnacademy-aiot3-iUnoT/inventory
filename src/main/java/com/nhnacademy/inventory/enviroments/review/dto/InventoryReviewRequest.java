package com.nhnacademy.inventory.enviroments.review.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryReviewRequest(

        @NotNull(message = "폐기 여부 입력은 필수 입니다.")
        Boolean isOut,

        @Size(max = 255, message = "메모는 최대 255자 까지 입니다.")
        String memo
) {
}
