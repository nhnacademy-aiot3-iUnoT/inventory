package com.nhnacademy.inventory.inventories.inventory.operation.outbound.controller;

import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundReason;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.service.MedicineOutboundService;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicineOutboundController.class)
class MedicineOutboundControllerTest extends SupportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MedicineOutboundService medicineOutboundService;

    @Test
    void outboundSuccess() throws Exception {
        MedicineOutboundRequest request = new MedicineOutboundRequest(1L, 10, 1L, OutboundReason.DISPENSING, null);

        mockMvc.perform(post("/api/core/inventories/{inventory-id}/outbound", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("inventory-outbound-create",
                        pathParameters(parameterWithName("inventory-id").description("출고 대상 재고 ID")),
                        requestFields(
                                fieldWithPath("medicinePackageUnitId").description("의약품 포장 단위 ID"),
                                fieldWithPath("quantity").description("출고 수량"),
                                fieldWithPath("zoneId").description("출고 구역 ID"),
                                fieldWithPath("reason").description("출고 사유"),
                                fieldWithPath("memo").description("상세 메모").optional(),
                                fieldWithPath("memoValid").ignored()
                        )
                ));
    }

    @Test
    @DisplayName("잠금 시간 초과")
    void outboundWithLockTimeout() throws Exception {
        MedicineOutboundRequest request = new MedicineOutboundRequest(
                1L,
                10,
                1L,
                OutboundReason.DISPENSING,
                null
        );

        willThrow(new PessimisticLockingFailureException("lock timeout"))
                .given(medicineOutboundService)
                .outbound(eq(1L), any(MedicineOutboundRequest.class));

        mockMvc.perform(post("/api/core/inventories/{inventory-id}/outbound", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("OD005"))
                .andExpect(jsonPath("$.error.message")
                        .value("다른 재고 처리 요청이 진행 중입니다. 잠시 후 다시 시도해주세요."));
    }
}
