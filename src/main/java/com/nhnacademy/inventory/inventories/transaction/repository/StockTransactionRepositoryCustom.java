package com.nhnacademy.inventory.inventories.transaction.repository;

import com.nhnacademy.inventory.inventories.transaction.dto.StockSummaryRow;
import com.nhnacademy.inventory.inventories.transaction.domain.StockTransaction;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchCondition;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StockTransactionRepositoryCustom {
    Page<StockTransactionSearchResponse> searchByCondition(
            Long zoneId, StockTransactionSearchCondition condition, Pageable pageable
    );

    List<StockTransaction> findTransactionsForStorageReport(
            Long storageId,
            Collection<TransactionType> types,
            LocalDateTime start,
            LocalDateTime end
    );

    List<StockSummaryRow> sumByType(
            List<Long> storageIds,
            Collection<TransactionType> types,
            LocalDateTime start,
            LocalDateTime end
    );
}
