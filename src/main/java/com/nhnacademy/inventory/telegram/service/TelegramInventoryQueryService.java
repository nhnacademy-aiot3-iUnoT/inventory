package com.nhnacademy.inventory.telegram.service;

import com.nhnacademy.inventory.chatbot.dto.ExpiringInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.LowStockInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.MedicineInventorySearchRow;
import com.nhnacademy.inventory.chatbot.dto.query.FindExpiringInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindLowStockInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.SearchMedicineInventoryQuery;
import com.nhnacademy.inventory.chatbot.repository.MedicineInventoryChatbotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 단톡방 챗봇의 재고 조회. 조회 제한값도 여기서 정한다.
 * 트랜잭션을 이 안에서 짧게 잡아, LLM 응답을 기다리는 동안 DB 커넥션을 붙잡지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TelegramInventoryQueryService {

    private static final int MAX_RESULTS = 10;
    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 90;
    private static final int DEFAULT_EXPIRING_DAYS = 30;

    private final MedicineInventoryChatbotRepository inventoryRepository;

    public List<MedicineInventorySearchRow> searchMedicineInventory(
            List<Long> storageIds,
            String keyword,
            String storageName,
            String zoneName
    ) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        log.info("[TelegramChatbot] 재고 조회 - keyword={}, storageName={}, zoneName={}", keyword, storageName, zoneName);

        return inventoryRepository.search(new SearchMedicineInventoryQuery(
                storageIds,
                keyword.trim(),
                storageName,
                zoneName,
                MAX_RESULTS
        ));
    }

    public List<ExpiringInventoryRow> findExpiringInventory(
            List<Long> storageIds,
            Integer searchDays,
            String medicineName,
            String storageName,
            String zoneName
    ) {
        int days = resolveSearchDays(searchDays);
        LocalDate today = LocalDate.now();

        log.info("[TelegramChatbot] 유통기한 임박 조회 - days={}", days);

        return inventoryRepository.findExpiring(new FindExpiringInventoryQuery(
                storageIds,
                today,
                today.plusDays(days),
                medicineName,
                storageName,
                zoneName,
                MAX_RESULTS
        ));
    }

    public List<LowStockInventoryRow> findLowStockInventory(
            List<Long> storageIds,
            String storageName,
            String zoneName
    ) {
        log.info("[TelegramChatbot] 재고 부족 조회 - storageName={}, zoneName={}", storageName, zoneName);

        return inventoryRepository.findLowStock(new FindLowStockInventoryQuery(
                storageIds,
                storageName,
                zoneName,
                MAX_RESULTS
        ));
    }

    private int resolveSearchDays(Integer searchDays) {
        if (searchDays == null) {
            return DEFAULT_EXPIRING_DAYS;
        }

        return Math.clamp(searchDays, MIN_DAYS, MAX_DAYS);
    }
}
