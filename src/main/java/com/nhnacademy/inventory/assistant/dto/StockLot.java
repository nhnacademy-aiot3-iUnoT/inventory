package com.nhnacademy.inventory.assistant.dto;

import java.time.LocalDate;

public record StockLot(
        LocalDate expirationDate,
        String lotNumber,
        Integer quantity
) {
}
