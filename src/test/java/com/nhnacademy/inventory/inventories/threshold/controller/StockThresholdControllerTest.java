package com.nhnacademy.inventory.inventories.threshold.controller;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdInfoResponse;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdSaveRequest;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdUpdateRequest;
import com.nhnacademy.inventory.inventories.threshold.exception.StockThresholdNotFoundException;
import com.nhnacademy.inventory.inventories.threshold.service.StockThresholdService;
import com.nhnacademy.inventory.medicines.medicine.exception.PackUnitNotFoundException;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.support.RestDocsUtils;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockThresholdController.class)
class StockThresholdControllerTest extends SupportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StockThresholdService stockThresholdService;

    @Nested
    @DisplayName("최소 재고 임계값 저장 POST /api/core/storages/{storage-id}/stock-thresholds")
    class saveStockThreshold {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            StockThresholdSaveRequest request = new StockThresholdSaveRequest(
                    11L, 10
            );
            StockThresholdInfoResponse response = new StockThresholdInfoResponse(
                    1L, 11L, 111L,
                    "테스트 약품", "테스트 단위", "테스트 조직", "테스트 저장소",
                    10, true
            );

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(stockThresholdInfoResponseFields("data."));

            given(stockThresholdService.saveStockThreshold(111L, request))
                    .willReturn(response);

            mockMvc.perform(post("/api/core/storages/{storage-id}/stock-thresholds", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.stockThreshold").value(10))
                    .andExpect(jsonPath("$.data.isActive").value(true))
                    .andDo(document("stock-threshold-save",
                            pathParameters(
                                    parameterWithName("storage-id").description("저장소 ID")
                            ),
                            requestFields(
                                    fieldWithPath("medicinePackageUnitId").type(JsonFieldType.NUMBER).description("단위 의약품 ID"),
                                    fieldWithPath("stockThreshold").type(JsonFieldType.NUMBER).description("최소 재고 임계값")
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            StockThresholdSaveRequest request = new StockThresholdSaveRequest(
                    11L, -10
            );

            mockMvc.perform(post("/api/core/storages/{storage-id}/stock-thresholds", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            StockThresholdSaveRequest request = new StockThresholdSaveRequest(
                    11L, 10
            );

            given(stockThresholdService.saveStockThreshold(111L, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(post("/api/core/storages/{storage-id}/stock-thresholds", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            StockThresholdSaveRequest request = new StockThresholdSaveRequest(
                    11L, 10
            );

            given(stockThresholdService.saveStockThreshold(111L, request))
                    .willThrow(new StorageNotFoundException());

            mockMvc.perform(post("/api/core/storages/{storage-id}/stock-thresholds", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));

        }

        @Test
        @DisplayName("실패 - 단위 의약품 없음")
        void fail_NotFoundMedicinePackageUnit() throws Exception {
            StockThresholdSaveRequest request = new StockThresholdSaveRequest(
                    11L, 10
            );

            given(stockThresholdService.saveStockThreshold(111L, request))
                    .willThrow(new PackUnitNotFoundException());

            mockMvc.perform(post("/api/core/storages/{storage-id}/stock-thresholds", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("최소 재고 임계값 목록 조회 GET /api/core/storages/{storage-id}/stock-thresholds")
    class getStockThresholds {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            StockThresholdInfoResponse response = new StockThresholdInfoResponse(
                    1L, 11L, 111L,
                    "테스트 약품", "테스트 단위", "테스트 조직", "테스트 저장소",
                    10, true
            );
            List<StockThresholdInfoResponse> responseList = List.of(response);

            given(stockThresholdService.getStockThresholds(111L))
                    .willReturn(responseList);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(stockThresholdInfoResponseFields("data[]."));

            mockMvc.perform(get("/api/core/storages/{storage-id}/stock-thresholds", 111L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].stockThreshold").value(10))
                    .andExpect(jsonPath("$.data[0].isActive").value(true))
                    .andDo(document("stock-threshold-get-list",
                            pathParameters(
                                    parameterWithName("storage-id").description("저장소 ID")
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("정상 처리 테스트(빈 배열)")
        void success_empty() throws Exception {
            given(stockThresholdService.getStockThresholds(111L))
                    .willReturn(List.of());

            mockMvc.perform(get("/api/core/storages/{storage-id}/stock-thresholds", 111L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            given(stockThresholdService.getStockThresholds(111L))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(get("/api/core/storages/{storage-id}/stock-thresholds", 111L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            given(stockThresholdService.getStockThresholds(111L))
                    .willThrow(new StorageNotFoundException());

            mockMvc.perform(get("/api/core/storages/{storage-id}/stock-thresholds", 111L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("최소 재고 임계값 수정 PUT /api/core/storages/{storage-id}/stock-thresholds/{stock-threshold-id}")
    class updateStockThreshold {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            StockThresholdUpdateRequest request = new StockThresholdUpdateRequest(
                    20, true
            );
            StockThresholdInfoResponse response = new StockThresholdInfoResponse(
                    1L, 11L, 111L,
                    "테스트 약품", "테스트 단위", "테스트 조직", "테스트 저장소",
                    20, true
            );

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(stockThresholdInfoResponseFields("data."));

            given(stockThresholdService.updateStockThreshold(111L, 1L, request))
                    .willReturn(response);

            mockMvc.perform(put("/api/core/storages/{storage-id}/stock-thresholds/{stock-threshold-id}", 111L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.stockThreshold").value(20))
                    .andExpect(jsonPath("$.data.isActive").value(true))
                    .andDo(document("stock-threshold-update",
                            pathParameters(
                                    parameterWithName("storage-id").description("저장소 ID"),
                                    parameterWithName("stock-threshold-id").description("최소 재고 임계값 ID")
                            ),
                            requestFields(
                                    fieldWithPath("stockThreshold").type(JsonFieldType.NUMBER).description("최소 재고 임계값"),
                                    fieldWithPath("isActive").type(JsonFieldType.BOOLEAN).description("활성화 상태")
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            StockThresholdUpdateRequest request = new StockThresholdUpdateRequest(
                    -20, true
            );

            mockMvc.perform(put("/api/core/storages/{storage-id}/stock-thresholds/{stock-threshold-id}", 111L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            StockThresholdUpdateRequest request = new StockThresholdUpdateRequest(
                    20, true
            );

            given(stockThresholdService.updateStockThreshold(111L, 1L, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(put("/api/core/storages/{storage-id}/stock-thresholds/{stock-threshold-id}", 111L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            StockThresholdUpdateRequest request = new StockThresholdUpdateRequest(
                    20, true
            );

            given(stockThresholdService.updateStockThreshold(111L, 1L, request))
                    .willThrow(new StorageNotFoundException());

            mockMvc.perform(put("/api/core/storages/{storage-id}/stock-thresholds/{stock-threshold-id}", 111L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 최소 재고 임계값 없음")
        void fail_NotFoundStockThreshold() throws Exception {
            StockThresholdUpdateRequest request = new StockThresholdUpdateRequest(
                    20, true
            );

            given(stockThresholdService.updateStockThreshold(111L, 1L, request))
                    .willThrow(new StockThresholdNotFoundException());

            mockMvc.perform(put("/api/core/storages/{storage-id}/stock-thresholds/{stock-threshold-id}", 111L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("최소 재고 임계값 삭제 DELETE /api/core/storages/{storage-id}/stock-thresholds/{stock-threshold-id}")
    class deleteStockThreshold {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/storages/{storage-id}/stock-thresholds/{stock-threshold-id}", 111L, 1L))
                    .andExpect(status().isNoContent())
                    .andDo(document("stock-threshold-delete",
                            pathParameters(
                                    parameterWithName("storage-id").description("저장소 ID"),
                                    parameterWithName("stock-threshold-id").description("최소 재고 임계값 ID")
                            )
                    ));

            verify(stockThresholdService).deleteStockThreshold(111L, 1L);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(stockThresholdService)
                    .deleteStockThreshold(111L, 1L);

            mockMvc.perform(delete("/api/core/storages/{storage-id}/stock-thresholds/{stock-threshold-id}", 111L, 1L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            willThrow(new StorageNotFoundException()).given(stockThresholdService)
                    .deleteStockThreshold(111L, 1L);

            mockMvc.perform(delete("/api/core/storages/{storage-id}/stock-thresholds/{stock-threshold-id}", 111L, 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 최소 재고 임계값 없음")
        void fail_NotFoundStockThreshold() throws Exception {
            willThrow(new PackUnitNotFoundException()).given(stockThresholdService)
                    .deleteStockThreshold(111L, 1L);

            mockMvc.perform(delete("/api/core/storages/{storage-id}/stock-thresholds/{stock-threshold-id}", 111L, 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    private List<FieldDescriptor> stockThresholdInfoResponseFields(String prefix){
        return List.of(
                fieldWithPath(prefix + "stockThresholdId").type(JsonFieldType.NUMBER).description("최소 재고 임계값 ID"),
                fieldWithPath(prefix + "medicinePackageUnitId").type(JsonFieldType.NUMBER).description("단위 의약품 ID"),
                fieldWithPath(prefix + "storageId").type(JsonFieldType.NUMBER).description("저장소 ID"),
                fieldWithPath(prefix + "productName").type(JsonFieldType.STRING).description("의약품 상품명"),
                fieldWithPath(prefix + "packUnit").type(JsonFieldType.STRING).description("포장 단위"),
                fieldWithPath(prefix + "organizationName").type(JsonFieldType.STRING).description("조직 이름"),
                fieldWithPath(prefix + "storageName").type(JsonFieldType.STRING).description("저장소 이름"),
                fieldWithPath(prefix + "stockThreshold").type(JsonFieldType.NUMBER).description("최소 재고 임계값"),
                fieldWithPath(prefix + "isActive").type(JsonFieldType.BOOLEAN).description("활성화 상태")
        );
    }
}
