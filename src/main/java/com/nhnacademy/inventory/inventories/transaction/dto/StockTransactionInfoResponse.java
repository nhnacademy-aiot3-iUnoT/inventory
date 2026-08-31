package com.nhnacademy.inventory.inventories.transaction.dto;

import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockTransactionInfoResponse(
        Long stockTransactionId,
        String medicineName,
        String packUnit,
        TransactionType transactionType,
        Integer quantity,
        String reason,
        String memo,
        UUID processedBy,

        // 계정 조회에 실패했거나 탈퇴한 계정이면 null -> 프론트에서 처리
        String processedByName,
        LocalDateTime processedAt
) {

    public static StockTransactionInfoResponse of(StockTransactionSearchResponse transaction, String processedByName) {
        return new StockTransactionInfoResponse(
                transaction.stockTransactionId(),
                transaction.medicineName(),
                transaction.packUnit(),
                transaction.transactionType(),
                transaction.quantity(),
                transaction.reason(),
                transaction.memo(),
                transaction.processedBy(),
                processedByName,
                transaction.processedAt()
        );
    }
}
