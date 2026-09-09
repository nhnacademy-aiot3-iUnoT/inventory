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
import com.nhnacademy.inventory.chatbot.repository.MedicineInventoryChatbotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("의약품 재고 챗봇 서비스 테스트")
class MedicineInventoryChatbotServiceTest {
    @Mock
    private MedicineInventoryChatbotRepository inventoryRepository;

    @Mock
    private ChatbotStorageAccessService accessService;

    @InjectMocks
    private MedicineInventoryChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        given(accessService.getAccessibleStorageIds()).willReturn(List.of(1L, 2L));
    }

    @Nested
    @DisplayName("의약품 재고 및 위치 조회")
    class SearchMedicineInventoryTest {
        @Test
        @DisplayName("검색 결과가 없으면 빈 목록을 반환한다")
        void returnEmptyItemsWhenNoInventoryExists() {
            given(inventoryRepository.search(any(SearchMedicineInventoryQuery.class))).willReturn(List.of());

            SearchMedicineInventoryResponse response = chatbotService.searchMedicineInventory(
                    " 타이레놀 ", null, null);

            assertTrue(response.items().isEmpty());
            verify(inventoryRepository).search(new SearchMedicineInventoryQuery(List.of(1L, 2L), "타이레놀", null, null, 10));
        }

        @Test
        @DisplayName("같은 포장 단위의 재고를 위치별로 묶어 반환한다")
        void groupInventoryByPackageUnit() {
            given(inventoryRepository.search(any(SearchMedicineInventoryQuery.class)))
                    .willReturn(List.of(searchRow("A창고", 80), searchRow("B창고", 20)));

            SearchMedicineInventoryResponse response = chatbotService.searchMedicineInventory(
                    "타이레놀", null, null);

            assertEquals(100, response.items().getFirst().totalQuantity());
            assertEquals(2, response.items().getFirst().locations().size());
        }
    }

    @Nested
    @DisplayName("유통기한 임박 제품 조회")
    class ExpiringInventoryTest {
        @Test
        @DisplayName("유통기한까지 남은 일수와 재고 정보를 반환한다")
        void returnExpiringInventory() {
            given(inventoryRepository.findExpiring(any(FindExpiringInventoryQuery.class)))
                    .willReturn(List.of(expiringRow(LocalDate.now().plusDays(10), 20)));

            ExpiringInventoryResponse response = chatbotService.getExpiringInventory(
                    30, "타이레놀", null, null);

            assertEquals(30, response.searchDays());
            assertEquals(10, response.items().getFirst().daysUntilExpiration());
        }

        @Test
        @DisplayName("조회 일수가 없으면 30일을 기본값으로 사용한다")
        void useDefaultDaysWhenDaysRemainingIsMissing() {
            given(inventoryRepository.findExpiring(any(FindExpiringInventoryQuery.class))).willReturn(List.of());

            ExpiringInventoryResponse response = chatbotService.getExpiringInventory(null, null, null, null);

            assertEquals(30, response.searchDays());
        }
    }

    @Nested
    @DisplayName("재고 부족 및 품절 제품 조회")
    class LowStockInventoryTest {
        @Test
        @DisplayName("현재 수량과 최소 재고를 반환한다")
        void returnCurrentQuantityAndThreshold() {
            given(inventoryRepository.findLowStock(any(FindLowStockInventoryQuery.class)))
                    .willReturn(List.of(lowStockRow(0, 10)));

            LowStockInventoryResponse response = chatbotService.getLowStockInventory(null, null);

            assertEquals(0, response.items().getFirst().currentQuantity());
            assertEquals(10, response.items().getFirst().threshold());
        }
    }

    private MedicineInventorySearchRow searchRow(String storageName, int quantity) {
        return new MedicineInventorySearchRow(
                1L, "타이레놀", "500mg",
                storageName, "2구역", quantity,
                "A창고".equals(storageName) ? 75L : 76L,
                "LOT-" + storageName,
                LocalDate.now().plusDays(20),
                com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus.NORMAL
        );
    }

    private ExpiringInventoryRow expiringRow(LocalDate expirationDate, int quantity) {
        return new ExpiringInventoryRow("타이레놀", "500mg", "LOT-1", expirationDate,
                quantity, "A창고", "2구역");
    }

    private LowStockInventoryRow lowStockRow(int quantity, int threshold) {
        return new LowStockInventoryRow("타이레놀", "500mg", quantity, threshold,
                "A창고", "2구역");
    }
}
