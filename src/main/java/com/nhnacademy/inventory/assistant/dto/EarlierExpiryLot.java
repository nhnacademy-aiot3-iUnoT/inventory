package com.nhnacademy.inventory.assistant.dto;

import java.time.LocalDate;

public record EarlierExpiryLot(
        LocalDate expirationDate,
        String lotNumber,
        Integer quantity
) {}
