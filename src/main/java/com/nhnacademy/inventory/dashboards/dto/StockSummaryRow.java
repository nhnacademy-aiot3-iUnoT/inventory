package com.nhnacademy.inventory.dashboards.dto;

import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.querydsl.core.annotations.QueryProjection;

/**
 * 거래 유형별 합계 한 줄. DB에서 집계해 그대로 받는다.
 */
public record StockSummaryRow(
        TransactionType transactionType,
        Long totalQuantity,
        Long txCount
) {
    @QueryProjection
    public StockSummaryRow {
    }

    public long quantityOrZero() {
        return totalQuantity == null ? 0L : totalQuantity;
    }

    public long countOrZero() {
        return txCount == null ? 0L : txCount;
    }
}
