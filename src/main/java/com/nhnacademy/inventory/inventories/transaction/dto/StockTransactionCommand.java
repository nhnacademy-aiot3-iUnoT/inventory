package com.nhnacademy.inventory.inventories.transaction.dto;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;

public record StockTransactionCommand (
        MedicinePackageUnit medicinePackageUnit,
        Zone zone,
        TransactionType transactionType,
        Integer quantity,
        String reason,
        String memo,
        byte[] processedBy
){
}
