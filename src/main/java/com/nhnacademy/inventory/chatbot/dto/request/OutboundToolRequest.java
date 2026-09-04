package com.nhnacademy.inventory.chatbot.dto.request;

import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundReason;

public record OutboundToolRequest(
        String medicineName,
        String packUnit,
        String storageName,
        String zoneName,
        Integer quantity,
        OutboundReason reason,
        String memo
) {
}