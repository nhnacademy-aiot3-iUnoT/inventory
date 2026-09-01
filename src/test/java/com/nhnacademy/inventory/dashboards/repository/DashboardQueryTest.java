package com.nhnacademy.inventory.dashboards.repository;

import com.nhnacademy.inventory.dashboards.dto.StockSummaryRow;
import com.nhnacademy.inventory.global.config.QuerydslConfig;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventoryResponse;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.repository.StockTransactionRepository;
import com.nhnacademy.inventory.organizations.department.domain.StorageDepartment;
import com.nhnacademy.inventory.organizations.department.repository.StorageDepartmentRepository;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 대시보드가 쓰는 조회 쿼리들이 실제로 동작하는지 확인한다.
 */
@DataJpaTest
@Import(QuerydslConfig.class)
@Sql("/sql/inventory-test-data.sql")
class DashboardQueryTest {

    @Autowired
    StorageDepartmentRepository storageDepartmentRepository;

    @Autowired
    StorageRepository storageRepository;

    @Autowired
    StockTransactionRepository stockTransactionRepository;

    @Autowired
    MedicineInventoryRepository medicineInventoryRepository;

    @Test
    @DisplayName("부서 목록으로 담당 저장소를 세는 쿼리가 동작한다.")
    void findAllByDepartmentIdIn() {
        List<StorageDepartment> storageDepartments =
                storageDepartmentRepository.findAllByDepartmentIdIn(List.of(1L, 2L));

        assertAll(
                () -> assertEquals(3, storageDepartments.size()),
                // 대시보드가 부서별 개수를 셀 때 department 를 지연 로딩으로 읽는다
                () -> assertTrue(storageDepartments.stream()
                        .allMatch(sd -> sd.getDepartment().getId() != null))
        );
    }

    @Test
    @DisplayName("부서의 담당 저장소 조회가 동작한다.")
    void findAllWithStorageByDepartmentId() {
        List<Long> storageIds = storageDepartmentRepository.findAllWithStorageByDepartmentId(1L)
                .stream()
                .map(sd -> sd.getStorage().getId())
                .toList();

        assertEquals(List.of(1L, 2L), storageIds);
    }

    @Test
    @DisplayName("조직 전체 저장소 ID 조회가 동작한다.")
    void findIdsByOrganizationIdAndStatusNot() {
        List<Long> storageIds =
                storageRepository.findIdsByOrganizationIdAndStatusNot(1L, StorageStatus.CLOSED);

        assertFalse(storageIds.isEmpty());
    }

    @Test
    @DisplayName("거래가 없는 기간을 집계하면 빈 목록이 온다.")
    void sumByType_NoTransaction() {
        List<StockSummaryRow> rows = stockTransactionRepository.sumByType(
                List.of(1L, 2L),
                List.of(TransactionType.INBOUND, TransactionType.OUTBOUND),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay()
        );

        assertTrue(rows.isEmpty());
    }

    @Test
    @DisplayName("과거 전체 기간으로 집계해도 쿼리가 성공한다.")
    void sumByType_WideRange() {
        List<StockSummaryRow> rows = stockTransactionRepository.sumByType(
                List.of(1L, 2L, 3L),
                List.of(TransactionType.INBOUND, TransactionType.OUTBOUND),
                LocalDateTime.of(2020, 1, 1, 0, 0),
                LocalDateTime.of(2030, 1, 1, 0, 0)
        );

        assertTrue(rows.stream().allMatch(row -> row.transactionType() != null));
    }

    @Test
    @DisplayName("임박 품목 수 집계가 동작한다.")
    void countExpiringWithinDays() {
        // 시드 데이터의 가장 이른 유통기한은 2026-10-31 이다
        long farFuture = medicineInventoryRepository.countExpiringWithinDays(
                1L, List.of(1L, 2L), 3650);
        long today = medicineInventoryRepository.countExpiringWithinDays(
                1L, List.of(1L, 2L), 0);

        assertAll(
                () -> assertTrue(farFuture > 0L),
                () -> assertEquals(0L, today)
        );
    }

    @Test
    @DisplayName("저장소 목록이 비어 있으면 빈 결과를 돌려준다.")
    void countExpiringWithinDays_EmptyStorages() {
        assertEquals(0L, medicineInventoryRepository.countExpiringWithinDays(1L, List.of(), 30));
    }

    @Test
    @DisplayName("부서 기준 임박 목록 조회가 동작한다.")
    void findExpiringInventoriesByStorageIds() {
        Page<ExpiringInventoryResponse> page =
                medicineInventoryRepository.findExpiringInventoriesByStorageIds(
                        1L, List.of(1L, 2L), 3650, PageRequest.of(0, 5));

        assertAll(
                () -> assertFalse(page.getContent().isEmpty()),
                () -> assertTrue(page.getTotalElements() > 0L),
                () -> assertTrue(page.getContent().stream()
                        .allMatch(item -> item.storageName() != null && item.medicineName() != null)),
                // 유통기한 오름차순
                () -> assertTrue(isSortedByExpiration(page.getContent()))
        );
    }

    private boolean isSortedByExpiration(List<ExpiringInventoryResponse> items) {
        for (int i = 1; i < items.size(); i++) {
            if (items.get(i - 1).expirationDate().isAfter(items.get(i).expirationDate())) {
                return false;
            }
        }

        return true;
    }
}
