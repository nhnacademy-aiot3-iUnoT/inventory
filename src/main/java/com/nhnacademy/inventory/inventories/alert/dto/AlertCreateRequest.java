package com.nhnacademy.inventory.inventories.alert.dto;

import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlertCreateRequest(
        @NotNull(message = "조직 아이디는 필수입니다.")
        Long organizationId,

        @NotNull(message = "알림 타입은 필수입니다.")
        AlertType alertType,

        @Size(max = 255, message = "알림 메시지는 최대 255자 입니다.")
        String message
) {
}
