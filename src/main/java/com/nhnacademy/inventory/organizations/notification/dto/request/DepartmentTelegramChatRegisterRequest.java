package com.nhnacademy.inventory.organizations.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentTelegramChatRegisterRequest(
        @NotBlank(message = "단톡방 chat id를 입력해주세요.")
        @Size(max = 255, message = "단톡방 chat id는 255자를 넘을 수 없습니다.")
        String chatId,

        Boolean enabled
) {
}
