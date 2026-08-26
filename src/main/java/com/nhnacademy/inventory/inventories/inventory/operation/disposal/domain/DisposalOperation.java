package com.nhnacademy.inventory.inventories.inventory.operation.disposal.domain;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.operation.disposal.dto.MedicineDisposalRequest;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionCommand;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DisposalOperation {

    private final StockTransactionService stockTransactionService;

    public void process(
            MedicineInventory inventory,
            MedicineDisposalRequest request
    ) {
        inventory.disposeQuantity(request.quantity());

        String memo = request.reason() == DisposalReason.OTHER
                ? request.memo().trim()
                : null;

        StockTransactionCommand command =
                new StockTransactionCommand(
                        inventory.getMedicinePackageUnit(),
                        inventory.getZone(),
                        TransactionType.DISPOSAL,
                        request.quantity(),
                        request.reason().name(),
                        memo,
                        UserContext.getUserUuid()
                );

        stockTransactionService.createStockTransaction(command);
    }
}
