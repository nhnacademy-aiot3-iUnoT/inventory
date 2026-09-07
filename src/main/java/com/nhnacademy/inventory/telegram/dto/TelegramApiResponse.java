package com.nhnacademy.inventory.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramApiResponse(
        boolean ok,
        String description
) {
}
