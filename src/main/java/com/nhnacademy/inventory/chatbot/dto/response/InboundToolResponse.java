package com.nhnacademy.inventory.chatbot.dto.response;

import java.time.LocalDate;

public record InboundToolResponse(
        boolean success,
        String message,
        String medicineName,
        String packUnit,
        String storageName,
        String zoneName,
        String lotNumber,
        LocalDate expirationDate,
        Integer quantity
) {
    public static InboundToolResponse success(
            String medicineName,
            String packUnit,
            String storageName,
            String zoneName,
            String lotNumber,
            LocalDate expirationDate,
            Integer quantity
    ) {
        return new InboundToolResponse(
                true,
                "입고 처리가 완료되었습니다.",
                medicineName,
                packUnit,
                storageName,
                zoneName,
                lotNumber,
                expirationDate,
                quantity
        );
    }

    public static InboundToolResponse failure(String message) {
        return new InboundToolResponse(false, message, null, null, null, null, null, null, null);
    }
}
