package com.nhnacademy.inventory.inventories.transaction.service;

import com.nhnacademy.inventory.inventories.transaction.domain.StockTransaction;
import com.nhnacademy.inventory.inventories.transaction.dto.StockSummaryRow;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionCommand;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchCondition;
import com.nhnacademy.inventory.global.client.AccountClient;
import com.nhnacademy.inventory.global.dto.account.AccountResponse;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionInfoResponse;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionSearchResponse;
import com.nhnacademy.inventory.inventories.transaction.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTransactionService {
    private final StockTransactionRepository stockTransactionRepository;
    private final AccountClient accountClient;

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

    public Page<StockTransactionInfoResponse> searchTransactionByCondition(
            Long zoneId, StockTransactionSearchCondition condition, Pageable pageable
    ){
        Page<StockTransactionSearchResponse> transactions =
                stockTransactionRepository.searchByCondition(zoneId, condition, pageable);

        Map<UUID, String> names = loadProcessorNames(transactions.getContent());

        return transactions.map(transaction ->
                StockTransactionInfoResponse.of(transaction, names.get(transaction.processedBy())));
    }

    private Map<UUID, String> loadProcessorNames(List<StockTransactionSearchResponse> transactions) {
        List<UUID> uuids = transactions.stream()
                .map(StockTransactionSearchResponse::processedBy)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (uuids.isEmpty()) {
            return Map.of();
        }

        try {
            List<AccountResponse> accounts = accountClient.findByUuids(uuids);

            if (accounts == null) {
                return Map.of();
            }

            return accounts.stream()
                    .filter(account -> account.accountUuid() != null && account.name() != null)
                    .collect(Collectors.toMap(
                            AccountResponse::accountUuid, AccountResponse::name, (a, b) -> a));
        } catch (Exception e) {
            log.warn("처리자 정보를 불러오지 못해 재고 변동 내역만 반환합니다. 처리자 수={}", uuids.size(), e);
            return Map.of();
        }
    }

/**
     * 저장소 여러 곳의 거래 유형별 합계. 대시보드 KPI 처럼 숫자만 필요할 때 쓴다.
     */
    public List<StockSummaryRow> sumByType(
            List<Long> storageIds,
            Collection<TransactionType> types,
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (storageIds.isEmpty()) {
            return List.of();
        }

        return stockTransactionRepository.sumByType(storageIds, types, start, end);
    }

    public List<StockTransaction> findTransactionsForStorageReport(Long storageId, List<TransactionType> types, LocalDate start, LocalDate end) {
        return stockTransactionRepository.findTransactionsForStorageReport(
                storageId,
                types,
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay());
    }
}
