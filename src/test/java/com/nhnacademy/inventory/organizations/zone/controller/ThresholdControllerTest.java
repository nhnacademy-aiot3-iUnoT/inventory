package com.nhnacademy.inventory.organizations.zone.controller;

import com.nhnacademy.inventory.global.error.GlobalExceptionHandler;
import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ThresholdSaveRequest;
import com.nhnacademy.inventory.organizations.zone.dto.ZoneCreateRequest;
import com.nhnacademy.inventory.organizations.zone.dto.ZoneInfoResponse;
import com.nhnacademy.inventory.organizations.zone.exception.ThresholdInvalidRangeException;
import com.nhnacademy.inventory.organizations.zone.exception.ThresholdNotFoundException;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNameAlreadyExistsException;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNotFoundException;
import com.nhnacademy.inventory.organizations.zone.service.ThresholdService;
import com.nhnacademy.inventory.organizations.zone.service.ZoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ThresholdController.class)
@Import(GlobalExceptionHandler.class)
class ThresholdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThresholdService thresholdService;

    private UUID accountUuid;

    @BeforeEach
    void setUp() {
        accountUuid = UUID.randomUUID();
    }

    @Nested
    @DisplayName("임계값 저장 PUT /api/core/zones/{zoneId}/zone-threshold")
    class saveZoneThreshold {
        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            ThresholdSaveRequest request = new ThresholdSaveRequest(
                    111L, BigDecimal.valueOf(20), BigDecimal.valueOf(30), 5);
            ThresholdInfoResponse response = new ThresholdInfoResponse(
                    1L, 11L, 111L,
                    BigDecimal.valueOf(20), BigDecimal.valueOf(30), 5
            );

            given(thresholdService.saveThreshold(11L, accountUuid, request)).willReturn(response);

            mockMvc.perform(put("/api/core/zones/{zoneId}/zone-threshold", 11L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.minValue").value(20))
                    .andExpect(jsonPath("$.data.maxValue").value(30));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            ThresholdSaveRequest request = new ThresholdSaveRequest(
                    null, BigDecimal.valueOf(20), BigDecimal.valueOf(30), 5);

            mockMvc.perform(put("/api/core/zones/{zoneId}/zone-threshold", 11L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            ThresholdSaveRequest request = new ThresholdSaveRequest(
                    111L, BigDecimal.valueOf(20), BigDecimal.valueOf(30), 5);

            given(thresholdService.saveThreshold(11L, accountUuid, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(put("/api/core/zones/{zoneId}/zone-threshold", 11L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 구역 없음")
        void fail_NotFoundZone() throws Exception {
            ThresholdSaveRequest request = new ThresholdSaveRequest(
                    111L, BigDecimal.valueOf(20), BigDecimal.valueOf(30), 5);

            given(thresholdService.saveThreshold(11L, accountUuid, request))
                    .willThrow(new ZoneNotFoundException());

            mockMvc.perform(put("/api/core/zones/{zoneId}/zone-threshold", 11L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 입력 범위 오류")
        void fail_DuplicationName() throws Exception {
            ThresholdSaveRequest request = new ThresholdSaveRequest(
                    111L, BigDecimal.valueOf(20), BigDecimal.valueOf(30), 5);

            given(thresholdService.saveThreshold(11L, accountUuid, request))
                    .willThrow(new ThresholdInvalidRangeException());

            mockMvc.perform(put("/api/core/zones/{zoneId}/zone-threshold", 11L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("임계값 목록 조회 GET /api/core/zones/{zoneId}/zone-threshold")
    class getZoneThreshold {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            ThresholdInfoResponse response = new ThresholdInfoResponse(
                    1L, 11L, 111L,
                    BigDecimal.valueOf(20), BigDecimal.valueOf(30), 5
            );
            List<ThresholdInfoResponse> responseList = List.of(response);

            given(thresholdService.getThresholds(11L, accountUuid))
                    .willReturn(responseList);

            mockMvc.perform(get("/api/core/zones/{zoneId}/zone-threshold", 11L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].minValue").value(20))
                    .andExpect(jsonPath("$.data[0].maxValue").value(30));
        }

        @Test
        @DisplayName("정상 처리 테스트(빈 배열)")
        void success_empty() throws Exception {
            given(thresholdService.getThresholds(11L, accountUuid))
                    .willReturn(List.of());

            mockMvc.perform(get("/api/core/zones/{zoneId}/zone-threshold", 11L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            given(thresholdService.getThresholds(11L, accountUuid))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(get("/api/core/zones/{zoneId}/zone-threshold", 11L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 구역 없음")
        void fail_NotFoundZone() throws Exception {

            given(thresholdService.getThresholds(11L, accountUuid))
                    .willThrow(new ZoneNotFoundException());

            mockMvc.perform(get("/api/core/zones/{zoneId}/zone-threshold", 11L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("임계값 삭제 DELETE /api/core/zones/{zoneId}/zone-threshold/{zoneThresholdId}")
    class deleteZoneThreshold {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/zones/{zoneId}/zone-threshold/{zoneThresholdId}", 11L, 1L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isNoContent());

            verify(thresholdService).deleteThreshold(11L, 1L, accountUuid);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(thresholdService)
                    .deleteThreshold(11L, 1L, accountUuid);

            mockMvc.perform(delete("/api/core/zones/{zoneId}/zone-threshold/{zoneThresholdId}", 11L, 1L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 구역 없음")
        void fail_NotFoundStorage() throws Exception {
            willThrow(new ZoneNotFoundException()).given(thresholdService)
                    .deleteThreshold(11L, 1L, accountUuid);

            mockMvc.perform(delete("/api/core/zones/{zoneId}/zone-threshold/{zoneThresholdId}", 11L, 1L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 구역 없음")
        void fail_NotFoundZone() throws Exception {
            willThrow(new ThresholdNotFoundException()).given(thresholdService)
                    .deleteThreshold(11L, 1L, accountUuid);

            mockMvc.perform(delete("/api/core/zones/{zoneId}/zone-threshold/{zoneThresholdId}", 11L, 1L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}