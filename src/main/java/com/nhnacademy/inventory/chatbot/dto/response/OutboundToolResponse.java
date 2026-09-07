package com.nhnacademy.inventory.chatbot.dto.response;

import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundReason;

public record OutboundToolResponse(
        boolean success,
        String message,
        String medicineName,
        String packUnit,
        String storageName,
        String zoneName,
        Integer quantity,
        OutboundReason reason
) {
    public static OutboundToolResponse success(
            String medicineName,
            String packUnit,
            String storageName,
            String zoneName,
            Integer quantity,
            OutboundReason reason
    ) {
        return new OutboundToolResponse(
                true,
                "출고 처리가 완료되었습니다.",
                medicineName,
                packUnit,
                storageName,
                zoneName,
                quantity,
                reason
        );
    }

    public static OutboundToolResponse failure(String message) {
        return new OutboundToolResponse(false, message, null, null, null, null, null, null);
    }
}
