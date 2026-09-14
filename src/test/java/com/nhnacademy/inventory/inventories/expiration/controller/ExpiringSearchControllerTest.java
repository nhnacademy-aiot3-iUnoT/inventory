package com.nhnacademy.inventory.inventories.expiration.controller;

import com.nhnacademy.inventory.inventories.expiration.service.ExpirationSearchService;
import com.nhnacademy.inventory.inventories.expiration.dto.ExpiringInventoryResponse;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpiringSearchController.class)
class ExpiringSearchControllerTest extends SupportControllerTest {
    @MockitoBean ExpirationSearchService expirationSearchService;

    @Test
    void searchExpiringInventoriesReturnsPage() throws Exception {
        var response = new ExpiringInventoryResponse(1L, 2L, 3L, 4L, 5L, 6L, "타이레놀", "10정", "테스트 조직", "A 저장소", "A 구역", "LOT-001", LocalDate.of(2026, 12, 31), 20);
        given(expirationSearchService.searchExpiringInventories(any(), any(Pageable.class))).willReturn(new PageImpl<>(List.of(response)));
        mockMvc.perform(get("/api/core/expiring").param("storageId", "5").param("filterType", "ALL").param("sortDirection", "ASC"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].inventoryId").value(1))
                .andExpect(jsonPath("$.data.content[0].medicineName").value("타이레놀"));
    }
}
