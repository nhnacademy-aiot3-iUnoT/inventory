package com.nhnacademy.inventory.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramSendMessageRequest(
        @JsonProperty("chat_id")
        String chatId,

        String text
) {
}
