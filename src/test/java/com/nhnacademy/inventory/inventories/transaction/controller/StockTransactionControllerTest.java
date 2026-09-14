package com.nhnacademy.inventory.inventories.transaction.controller;

import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionInfoResponse;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockTransactionController.class)
class StockTransactionControllerTest extends SupportControllerTest {
    @MockitoBean StockTransactionService stockTransactionService;

    @Test
    void searchReturnsTransactionsForZone() throws Exception {
        var response = new StockTransactionInfoResponse(1L, "타이레놀", "10정", TransactionType.INBOUND, 20, "입고", "비고", UUID.randomUUID(), "홍길동", LocalDateTime.of(2026, 9, 14, 10, 0));
        given(stockTransactionService.searchTransactionByCondition(eq(1L), any(), any(Pageable.class))).willReturn(new PageImpl<>(List.of(response)));
        mockMvc.perform(get("/api/core/zones/{zone-id}/stock-transaction", 1L).param("medicineName", "타이레놀").param("transactionType", "INBOUND").param("startDate", "2026-09-01").param("endDate", "2026-09-14"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].stockTransactionId").value(1))
                .andExpect(jsonPath("$.data.content[0].transactionType").value("INBOUND"));
    }
}
