package com.nhnacademy.inventory.chatbot.service;

import com.nhnacademy.inventory.chatbot.dto.ExpiringInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.LowStockInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.MedicineInventorySearchRow;
import com.nhnacademy.inventory.chatbot.dto.query.FindExpiringInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindLowStockInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.SearchMedicineInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.response.ExpiringInventoryResponse;
import com.nhnacademy.inventory.chatbot.dto.response.LowStockInventoryResponse;
import com.nhnacademy.inventory.chatbot.dto.response.SearchMedicineInventoryResponse;
import com.nhnacademy.inventory.chatbot.dto.response.SearchMedicineInventoryResponse.Location;
import com.nhnacademy.inventory.chatbot.dto.response.SearchMedicineInventoryResponse.MedicineInventoryItem;
import com.nhnacademy.inventory.chatbot.repository.MedicineInventoryChatbotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineInventoryChatbotService {
    private static final int MAX_RESULTS = 10;
    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 90;
    private static final int DEFAULT_EXPIRING_DAYS = 30;

    private final MedicineInventoryChatbotRepository inventoryRepository;
    private final ChatbotStorageAccessService accessService;

    /**
     * 의약품명, 저장소/구역 -> 재고 조회
     */
    public SearchMedicineInventoryResponse searchMedicineInventory(String keyword, String storageName, String zoneName) {
        if (keyword == null || keyword.isBlank()) {
            return new SearchMedicineInventoryResponse(List.of());
        }

        String trimmedKeyword = keyword.trim();
        log.info("[Chatbot] 재고 조회 - keyword={}, storageName={}, zoneName={}", trimmedKeyword, storageName, zoneName);

        SearchMedicineInventoryQuery query = new SearchMedicineInventoryQuery(
                accessService.getAccessibleStorageIds(),
                trimmedKeyword,
                storageName,
                zoneName,
                MAX_RESULTS
        );

        // DB 조회 결과
        List<MedicineInventorySearchRow> rows = inventoryRepository.search(query);

        if (rows.isEmpty()) {
            log.info("[Chatbot] 재고 조회 결과 - rowCount=0, itemCount=0");
            return new SearchMedicineInventoryResponse(List.of());
        }

        // 의약품 포장 단위로 묶기
        Map<Long, List<MedicineInventorySearchRow>> groupedRows = new LinkedHashMap<>();
        rows.forEach(row -> groupedRows
                .computeIfAbsent(row.packageUnitId(), ignored -> new ArrayList<>())
                .add(row));

        // 챗봇 Tool 반환
        List<MedicineInventoryItem> items = groupedRows.values().stream()
                .map(this::toMedicineInventoryItem)
                .toList();

        log.info("[Chatbot] 재고 조회 결과 - rowCount={}, itemCount={}", rows.size(), items.size());

        return new SearchMedicineInventoryResponse(items);
    }

    /**
     * 유효기간 임박 의약품
     */
    public ExpiringInventoryResponse getExpiringInventory(Integer daysRemaining, String medicineName, String storageName, String zoneName) {
        int requestedDays = daysRemaining == null ? DEFAULT_EXPIRING_DAYS : daysRemaining;
        int days = Math.clamp(requestedDays, MIN_DAYS, MAX_DAYS);
        LocalDate today = LocalDate.now();

        log.info("[Chatbot] 유통기한 임박 조회 - days={}, medicineName={}, storageName={}, zoneName={}", days, medicineName, storageName, zoneName);

        FindExpiringInventoryQuery query = new FindExpiringInventoryQuery(
                accessService.getAccessibleStorageIds(),
                today,
                today.plusDays(days),
                medicineName,
                storageName,
                zoneName,
                MAX_RESULTS
        );

        List<ExpiringInventoryResponse.Item> items = inventoryRepository.findExpiring(query).stream()
                .map(row -> toExpiringInventoryItem(row, today))
                .toList();

        log.info("[Chatbot] 유통기한 임박 조회 결과 - itemCount={}", items.size());

        return new ExpiringInventoryResponse(days, items);
    }

    /**
     * 재고 < 최소 재고 의약품 조회
     */
    public LowStockInventoryResponse getLowStockInventory(String storageName, String zoneName) {
        log.info("[Chatbot] 저재고 조회 - storageName={}, zoneName={}", storageName, zoneName);

        FindLowStockInventoryQuery query = new FindLowStockInventoryQuery(
                accessService.getAccessibleStorageIds(),
                storageName,
                zoneName,
                MAX_RESULTS
        );

        List<LowStockInventoryResponse.Item> items = inventoryRepository.findLowStock(query).stream()
                .map(this::toLowStockInventoryItem)
                .toList();

        log.info("[Chatbot] 저재고 조회 결과 - itemCount={}", items.size());

        return new LowStockInventoryResponse(items);
    }

    private MedicineInventoryItem toMedicineInventoryItem(
            List<MedicineInventorySearchRow> rows
    ) {
        MedicineInventorySearchRow first = rows.getFirst();

        return new MedicineInventoryItem(
                first.productName(),
                first.packUnit(),
                rows.stream()
                        .mapToInt(MedicineInventorySearchRow::currentQuantity)
                        .sum(),
                rows.stream()
                        .map(row -> new Location(
                                row.storageName(),
                                row.zoneName(),
                                row.currentQuantity(),
                                row.inventoryId(),
                                row.lotNumber(),
                                row.expirationDate(),
                                row.managementStatus()
                        ))
                        .toList()
        );
    }

    private ExpiringInventoryResponse.Item toExpiringInventoryItem(ExpiringInventoryRow row, LocalDate today) {
        return new ExpiringInventoryResponse.Item(
                row.productName(),
                row.packUnit(),
                row.lotNumber(),
                row.expirationDate(),
                (int) ChronoUnit.DAYS.between(today, row.expirationDate()),
                row.currentQuantity(),
                row.storageName(),
                row.zoneName()
        );
    }

    private LowStockInventoryResponse.Item toLowStockInventoryItem(LowStockInventoryRow row) {
        return new LowStockInventoryResponse.Item(
                row.productName(),
                row.packUnit(),
                row.currentQuantity(),
                row.threshold(),
                row.storageName(),
                row.zoneName()
        );
    }
}
