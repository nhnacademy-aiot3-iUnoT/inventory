package com.nhnacademy.inventory.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramChat(
        Long id,
        String type
) {
    // 단톡방 여부. 개인 DM(private)은 부서를 특정할 수 없어 처리하지 않는다.
    public boolean isGroup() {
        return "group".equals(type) || "supergroup".equals(type);
    }
}
