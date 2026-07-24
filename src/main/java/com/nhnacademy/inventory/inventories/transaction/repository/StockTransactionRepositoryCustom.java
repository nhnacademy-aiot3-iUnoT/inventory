package com.nhnacademy.inventory.inventories.transaction.repository;

import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchCondition;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockTransactionRepositoryCustom {
    Page<StockTransactionSearchResponse> searchByCondition(
            Long zoneId, StockTransactionSearchCondition condition, Pageable pageable
    );
}
