package com.nhnacademy.inventory.chatbot.repository;

import com.nhnacademy.inventory.chatbot.dto.ConsumptionSummaryRow;
import com.nhnacademy.inventory.chatbot.dto.StockPositionRow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StockTransactionChatbotRepository {
    // 해당 저장소 출고량 조회
    List<ConsumptionSummaryRow> sumOutboundTransactions(
            List<Long> storageIds, LocalDateTime start, LocalDateTime end
    );

    // 해당 저장소 유통기한 만료로 인한 폐기량 조회
    List<ConsumptionSummaryRow> sumDisposalTransactions(
            List<Long> storageIds, LocalDateTime start, LocalDateTime end
    );

    // 해당 저장소 재고현황 조회
    List<StockPositionRow> findStockPositions(
            List<Long> storageIds, LocalDate expiryLimit);
}
