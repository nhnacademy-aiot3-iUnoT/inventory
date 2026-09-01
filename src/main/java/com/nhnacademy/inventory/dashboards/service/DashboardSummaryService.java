package com.nhnacademy.inventory.dashboards.service;

import com.nhnacademy.inventory.dashboards.dto.DashboardSummaryResponse;
import com.nhnacademy.inventory.dashboards.dto.DashboardSummaryResponse.MetricResponse;
import com.nhnacademy.inventory.dashboards.dto.StockSummaryRow;
import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import com.nhnacademy.inventory.inventories.alert.repository.AlertRepository;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.repository.StockTransactionRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 대시보드 KPI 4장을 집계한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardSummaryService {

    private static final List<TransactionType> KPI_TYPES =
            List.of(TransactionType.INBOUND, TransactionType.OUTBOUND);

    private static final int EXPIRING_WINDOW_DAYS = 30;

    private final DashboardScopeResolver scopeResolver;
    private final StockTransactionRepository stockTransactionRepository;
    private final MedicineInventoryRepository inventoryRepository;
    private final AlertRepository alertRepository;

    public DashboardSummaryResponse getSummary(Long departmentId, LocalDate date) {
        List<Long> storageIds = scopeResolver.resolveStorageIds(departmentId);
        OrganizationMember member = scopeResolver.getCurrentMember();

        if (storageIds.isEmpty()) {
            return DashboardSummaryResponse.empty();
        }

        Map<TransactionType, StockSummaryRow> today = sumByType(storageIds, date);
        Map<TransactionType, StockSummaryRow> prev = sumByType(storageIds, date.minusDays(1));

        return new DashboardSummaryResponse(
                metric(today, prev, TransactionType.INBOUND),
                metric(today, prev, TransactionType.OUTBOUND),
                expiringMetric(member.getOrganization().getId(), storageIds),
                alertMetric(member, date)
        );
    }

    private Map<TransactionType, StockSummaryRow> sumByType(List<Long> storageIds, LocalDate date) {
        return stockTransactionRepository.sumByType(
                        storageIds,
                        KPI_TYPES,
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()
                )
                .stream()
                .collect(Collectors.toMap(StockSummaryRow::transactionType, Function.identity()));
    }

    /** 거래가 없는 날은 집계 결과에 줄이 아예 없으므로 0으로 채운다. */
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

    /**
     * 임박 품목 수.
     * 과거 스냅샷 테이블이 없으므로 "전일 대비"는 현재 재고를 어제 기준(오늘+29일)으로 다시 세어 근사한다.
     * 어제 이후의 입·출고 변동은 반영되지 않는다.
     */
    private MetricResponse expiringMetric(Long organizationId, List<Long> storageIds) {
        long itemCount = inventoryRepository.countExpiringWithinDays(
                organizationId, storageIds, EXPIRING_WINDOW_DAYS);
        long prevCount = inventoryRepository.countExpiringWithinDays(
                organizationId, storageIds, EXPIRING_WINDOW_DAYS - 1);

        return MetricResponse.of(itemCount, 0L, prevCount);
    }

    /**
     * 환경 이탈 알림.
     * alerts 테이블은 조직 단위로만 쌓이므로(저장소 FK 없음) 부서로 좁히지 않고 조직 기준으로 센다.
     */
    private MetricResponse alertMetric(OrganizationMember member, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        long count = alertRepository.countByOrganizationAndAlertTypeAndCreatedAtBetween(
                member.getOrganization(), AlertType.ENV_WARNING, start, end);

        long unchecked = alertRepository.countByOrganizationAndAlertTypeAndIsCheckedAndCreatedAtBetween(
                member.getOrganization(), AlertType.ENV_WARNING, false, start, end);

        long prevCount = alertRepository.countByOrganizationAndAlertTypeAndCreatedAtBetween(
                member.getOrganization(), AlertType.ENV_WARNING, start.minusDays(1), start);

        return MetricResponse.of(count, unchecked, prevCount);
    }

}
