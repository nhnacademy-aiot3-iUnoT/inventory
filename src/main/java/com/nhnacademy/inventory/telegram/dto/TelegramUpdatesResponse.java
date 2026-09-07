package com.nhnacademy.inventory.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramUpdatesResponse(
        boolean ok,
        String description,
        List<TelegramUpdate> result
) {
}
