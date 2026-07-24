package com.nhnacademy.inventory.inventories.transaction.service;

import com.nhnacademy.inventory.inventories.transaction.domain.StockTransaction;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionCommand;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchCondition;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchResponse;
import com.nhnacademy.inventory.inventories.transaction.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTransactionService {
    private final StockTransactionRepository stockTransactionRepository;

    @Transactional
    public void createStockTransaction(StockTransactionCommand command){
        StockTransaction stockTransaction = StockTransaction.builder()
                .medicinePackageUnit(command.medicinePackageUnit())
                .zone(command.zone())
                .transactionType(command.transactionType())
                .quantity(command.quantity())
                .reason(command.reason())
                .memo(command.memo())
                .processedBy(command.processedBy())
                .build();

        stockTransactionRepository.save(stockTransaction);
    }

    public Page<StockTransactionSearchResponse> searchTransactionByCondition(
            Long zoneId, StockTransactionSearchCondition condition, Pageable pageable
    ){
        return stockTransactionRepository.searchByCondition(zoneId, condition, pageable);
    }
}
