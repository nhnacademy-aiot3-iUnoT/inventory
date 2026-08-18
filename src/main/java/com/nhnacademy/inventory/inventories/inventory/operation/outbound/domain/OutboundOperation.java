package com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.exception.InsufficientStockException;
import com.nhnacademy.inventory.inventories.inventory.exception.InvalidOutboundTypeException;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionCommand;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboundOperation {

    private final StockTransactionService stockTransactionService;

    public void process(
            Zone zone,
            List<MedicineInventory> inventories,
            MedicineOutboundRequest request,
            UUID processedBy
    ) {

        if (request.transactionType() != TransactionType.OUTBOUND
                && request.transactionType() != TransactionType.TRANSFER_OUT) {
            throw new InvalidOutboundTypeException();
        }

        int totalQuantity = 0;

        for (MedicineInventory inventory : inventories) {
            totalQuantity += inventory.getCurrentQuantity();
        }

        if (totalQuantity < request.quantity()) {
            throw new InsufficientStockException();
        }

        int remainingQuantity = request.quantity();

        for (MedicineInventory inventory : inventories) {
            if (remainingQuantity == 0) {
                break;
            }

            int inventoryQuantity = inventory.getCurrentQuantity();

            if (inventoryQuantity >= remainingQuantity) {
                inventory.decreaseQuantity(remainingQuantity);
                remainingQuantity = 0;
            } else {
                inventory.decreaseQuantity(inventoryQuantity);
                remainingQuantity -= inventoryQuantity;
            }
        }

        MedicinePackageUnit medicinePackageUnit =
                inventories.getFirst().getMedicinePackageUnit();

        StockTransactionCommand command =
                new StockTransactionCommand(
                        medicinePackageUnit,
                        zone,
                        request.transactionType(),
                        request.quantity(),
                        null,
                        request.memo(),
                        processedBy
                );
        stockTransactionService.createStockTransaction(command);
    }
}
