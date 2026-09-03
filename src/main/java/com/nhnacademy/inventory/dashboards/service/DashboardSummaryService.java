package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.dto.DashboardSummaryResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardSummaryResponse.MetricResponse;
import com.nhnacademy.inventory.inventories.transaction.dto.StockSummaryRow;
import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import com.nhnacademy.inventory.inventories.alert.service.AlertService;
import com.nhnacademy.inventory.inventories.expiration.service.ExpirationSearchService;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardSummaryService {

    private static final List<TransactionType> KPI_TYPES =
            List.of(TransactionType.INBOUND, TransactionType.OUTBOUND); // 입고,출고만 타입만 조회 대상

    private static final int EXPIRING_WINDOW_DAYS = 30;

    private final DashboardScopeResolver scopeResolver;
    private final StockTransactionService stockTransactionService;
    private final ExpirationSearchService expirationSearchService;
    private final AlertService alertService;

    public DashboardSummaryResponse getSummary(Long departmentId, LocalDate date) {
        List<Long> storageIds = scopeResolver.resolveStorageIds(departmentId); // 해당 저장소 접근가능한지 검증및 저장소 조회

        if (storageIds.isEmpty()) { // 저장소 조회 실패시 빈리스트 반환
            return DashboardSummaryResponse.empty();
        }

        Map<TransactionType, StockSummaryRow> today = sumByType(storageIds, date); // 오늘 일어난 입출고 조회
        Map<TransactionType, StockSummaryRow> prev = sumByType(storageIds, date.minusDays(1)); // 어제 일어난 입출고 조회

        return new DashboardSummaryResponse(
                metric(today, prev, TransactionType.INBOUND),
                metric(today, prev, TransactionType.OUTBOUND),
                expiringMetric(storageIds),
                alertMetric(date)
        );
    }

    // 특정 날짜의 입출고 집계
    private Map<TransactionType, StockSummaryRow> sumByType(List<Long> storageIds, LocalDate date) {
        return stockTransactionService.sumByType(
                        storageIds,
                        KPI_TYPES,
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()
                )
                .stream()
                .collect(Collectors.toMap(StockSummaryRow::transactionType, Function.identity()));
    }

    // 값이 없는경우 건수가 0으로 설정
    private MetricResponse metric(
            Map<TransactionType, StockSummaryRow> today,
            Map<TransactionType, StockSummaryRow> prev,
            TransactionType type
    ) {
        StockSummaryRow todayRow = today.get(type);
        StockSummaryRow prevRow = prev.get(type);

        return MetricResponse.of(
                todayRow == null ? 0L : todayRow.quantityOrZero(),
                todayRow == null ? 0L : todayRow.countOrZero(),
                prevRow == null ? 0L : prevRow.quantityOrZero()
        );
    }

    // 유통기한 임박 품목 조회
    private MetricResponse expiringMetric(List<Long> storageIds) {
        long itemCount = expirationSearchService.countExpiringByStorages(
                storageIds, EXPIRING_WINDOW_DAYS);
        long prevCount = expirationSearchService.countExpiringByStorages(
                storageIds, EXPIRING_WINDOW_DAYS - 1);

        return MetricResponse.of(itemCount, 0L, prevCount);
    }

    // 환경 이탈 알림 조회
    private MetricResponse alertMetric(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        long count = alertService.countAlerts(AlertType.ENV_WARNING, start, end);
        long unchecked = alertService.countUncheckedAlerts(AlertType.ENV_WARNING, start, end);
        long prevCount = alertService.countAlerts(AlertType.ENV_WARNING, start.minusDays(1), start);

        return MetricResponse.of(count, unchecked, prevCount);
    }

}
