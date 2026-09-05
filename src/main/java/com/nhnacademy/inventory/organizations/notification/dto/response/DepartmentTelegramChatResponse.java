package com.nhnacademy.inventory.organizations.notification.dto.response;

import com.nhnacademy.inventory.organizations.notification.domain.DepartmentTelegramChat;

public record DepartmentTelegramChatResponse(
        Long departmentTelegramChatId,
        Long departmentId,
        String departmentName,
        String chatId,
        boolean enabled
) {
    public static DepartmentTelegramChatResponse from(DepartmentTelegramChat chat) {
        return new DepartmentTelegramChatResponse(
                chat.getDepartmentTelegramChatId(),
                chat.getDepartment().getId(),
                chat.getDepartment().getName(),
                chat.getChatId(),
                Boolean.TRUE.equals(chat.getIsEnabled())
        );
    }
}
