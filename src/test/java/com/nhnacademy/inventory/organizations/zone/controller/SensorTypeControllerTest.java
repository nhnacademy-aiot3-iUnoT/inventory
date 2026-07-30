package com.nhnacademy.inventory.organizations.zone.controller;

import com.nhnacademy.inventory.global.error.GlobalExceptionHandler;
import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import com.nhnacademy.inventory.organizations.zone.dto.SensorTypeCreateRequest;
import com.nhnacademy.inventory.organizations.zone.dto.SensorTypeInfoResponse;
import com.nhnacademy.inventory.organizations.zone.dto.ZoneCreateRequest;
import com.nhnacademy.inventory.organizations.zone.dto.ZoneInfoResponse;
import com.nhnacademy.inventory.organizations.zone.exception.SensorTypeNameAlreadyExistsException;
import com.nhnacademy.inventory.organizations.zone.exception.SensorTypeNotFoundException;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNameAlreadyExistsException;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNotFoundException;
import com.nhnacademy.inventory.organizations.zone.service.SensorTypeService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorTypeController.class)
@Import(GlobalExceptionHandler.class)
class SensorTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SensorTypeService sensorTypeService;

    @Nested
    @DisplayName("센서타입 생성 POST /api/core/sensor-type")
    class createSensorType {
        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            SensorTypeCreateRequest request = new SensorTypeCreateRequest("테스트 이름", "테스트 설명");
            SensorTypeInfoResponse response = new SensorTypeInfoResponse(1L, "테스트 이름", "테스트 설명");

            given(sensorTypeService.createSensorType(request)).willReturn(response);

            mockMvc.perform(post("/api/core/sensor-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("테스트 이름"))
                    .andExpect(jsonPath("$.data.description").value("테스트 설명"));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            SensorTypeCreateRequest request = new SensorTypeCreateRequest("", "테스트 설명");

            mockMvc.perform(post("/api/core/sensor-types", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 중복 이름")
        void fail_DuplicationName() throws Exception {
            SensorTypeCreateRequest request = new SensorTypeCreateRequest("테스트 이름", "테스트 설명");

            given(sensorTypeService.createSensorType(request))
                    .willThrow(new SensorTypeNameAlreadyExistsException());

            mockMvc.perform(post("/api/core/sensor-types", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("센서타입 목록 조회 GET /api/core/sensor-types")
    class getSensorType {
        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            SensorTypeInfoResponse response = new SensorTypeInfoResponse(1L, "테스트 이름", "테스트 설명");
            List<SensorTypeInfoResponse> responseList = List.of(response);

            given(sensorTypeService.getSensorTypes()).willReturn(responseList);

            mockMvc.perform(get("/api/core/sensor-types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("테스트 이름"))
                    .andExpect(jsonPath("$.data[0].description").value("테스트 설명"));
        }

        @Test
        @DisplayName("정상 처리 테스트 (빈 리스트)")
        void success_empty() throws Exception {
            given(sensorTypeService.getSensorTypes()).willReturn(List.of());

            mockMvc.perform(get("/api/core/sensor-types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("센서타입 삭제 DELETE /api/core/sensor-types/{sensorTypeId}")
    class deleteSensorType {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/sensor-types/{sensorTypeId}", 1L))
                    .andExpect(status().isNoContent());

            verify(sensorTypeService).deleteSensorType(1L);
        }

        @Test
        @DisplayName("실패 - 구역 없음")
        void fail_NotFoundZone() throws Exception {
            willThrow(new SensorTypeNotFoundException()).given(sensorTypeService)
                    .deleteSensorType(1L);

            mockMvc.perform(delete("/api/core/sensor-types/{sensorTypeId}", 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}