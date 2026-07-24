package com.nhnacademy.inventory.inventories.transaction.dto;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;

public record StockTransactionCommand (
        MedicineInventory medicineInventory,
        TransactionType transactionType,
        Integer quantity,
        Integer before_quantity,
        Integer after_quantity,
        String reason,
        String memo,
        byte[] processedBy
){
}
