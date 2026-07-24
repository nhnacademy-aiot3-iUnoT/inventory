package com.nhnacademy.inventory.inventories.transaction.dto;

import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;

import java.time.LocalDate;

public record StockTransactionSearchCondition(
        String medicineName,
        TransactionType transactionType,
        LocalDate startDate,
        LocalDate endDate
) {
}
