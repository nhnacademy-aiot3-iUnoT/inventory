package com.nhnacademy.inventory.inventories.transaction.dto;

import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockTransactionSearchResponse (
        Long stockTransactionId,
        String medicineName,
        String packUnit,
        TransactionType transactionType,
        Integer quantity,
        String reason,
        String memo,
        UUID processedBy,
        LocalDateTime processedAt
){
}
