package com.nhnacademy.inventory.organizations.sensor.controller;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.sensor.dto.*;
import com.nhnacademy.inventory.organizations.sensor.exception.ZoneSensorAlreadyExistsException;
import com.nhnacademy.inventory.organizations.sensor.exception.ZoneSensorNotFoundException;
import com.nhnacademy.inventory.organizations.sensor.service.ZoneSensorService;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNotFoundException;
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
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ZoneSensorController.class)
class ZoneSensorControllerTest extends SupportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ZoneSensorService zoneSensorService;

    @Nested
    @DisplayName("구역 센서 생성 POST /api/core/zones/{zone-id}/zone-sensors")
    class createZoneSensor {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            ZoneSensorCreateRequest request = new ZoneSensorCreateRequest(
                    "테스트 디바이스", "테스트 이름", "테스트 설명");
            ZoneSensorDetailResponse response = new ZoneSensorDetailResponse(
                    1L, 11L, 111L,
                    "테스트 디바이스", "테스트 조직", "테스트 저장소",
                    "테스트 구역", "테스트 이름", "테스트 설명");

            given(zoneSensorService.createZoneSensor(111L, request))
                    .willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(zoneSensorDetailResponseFields("data."));

            mockMvc.perform(post("/api/core/zones/{zone-id}/zone-sensors", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.deviceEui").value("테스트 디바이스"))
                    .andExpect(jsonPath("$.data.name").value("테스트 이름"))
                    .andExpect(jsonPath("$.data.description").value("테스트 설명"))
                    .andDo(document("zone-sensor-create",
                            pathParameters(
                                    parameterWithName("zone-id").description("저장소 ID")
                            ),
                            requestFields(
                                    fieldWithPath("deviceEui").type(JsonFieldType.STRING).description("등록할 센서 ID(필수)"),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("등록할 센서 이름(필수)"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("등록할 센서 설명(선택)").optional()
                            ),
                            responseFields(
                                    responseFields
                            )));

        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            ZoneSensorCreateRequest request = new ZoneSensorCreateRequest(
                    "", "테스트 이름", "테스트 설명");

            mockMvc.perform(post("/api/core/zones/{zone-id}/zone-sensors", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            ZoneSensorCreateRequest request = new ZoneSensorCreateRequest(
                    "테스트 디바이스", "테스트 이름", "테스트 설명");

            given(zoneSensorService.createZoneSensor(111L, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(post("/api/core/zones/{zone-id}/zone-sensors", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 구역")
        void fail_NotFoundZone() throws Exception {
            ZoneSensorCreateRequest request = new ZoneSensorCreateRequest(
                    "테스트 디바이스", "테스트 이름", "테스트 설명");

            given(zoneSensorService.createZoneSensor(111L, request))
                    .willThrow(new ZoneNotFoundException());

            mockMvc.perform(post("/api/core/zones/{zone-id}/zone-sensors", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 중복 등록")
        void fail_DuplicationDevice() throws Exception {
            ZoneSensorCreateRequest request = new ZoneSensorCreateRequest(
                    "테스트 디바이스", "테스트 이름", "테스트 설명");

            given(zoneSensorService.createZoneSensor(111L, request))
                    .willThrow(new ZoneSensorAlreadyExistsException());

            mockMvc.perform(post("/api/core/zones/{zone-id}/zone-sensors", 111L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 센서 조회 GET /api/core/zones/{zone-id}/zone-sensors")
    class getZoneSensors {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            ZoneSensorInfoResponse response = new ZoneSensorInfoResponse(
                    1L, 111L, "테스트 디바이스", "테스트 이름");

            given(zoneSensorService.getZoneSensors(111L))
                    .willReturn(List.of(response));

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(zoneSensorInfoResponseFields("data[]."));

            mockMvc.perform(get("/api/core/zones/{zone-id}/zone-sensors", 111L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].deviceEui").value("테스트 디바이스"))
                    .andExpect(jsonPath("$.data[0].name").value("테스트 이름"))
                    .andDo(document("zone-sensor-get-list",
                            pathParameters(
                                    parameterWithName("zone-id").description("저장소 ID")
                            ),
                            responseFields(
                                    responseFields
                            )));
        }

        @Test
        @DisplayName("정상 처리 테스트(빈 배열)")
        void success_empty() throws Exception {
            given(zoneSensorService.getZoneSensors(111L))
                    .willReturn(List.of());

            mockMvc.perform(get("/api/core/zones/{zone-id}/zone-sensors", 111L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            given(zoneSensorService.getZoneSensors(111L))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(get("/api/core/zones/{zone-id}/zone-sensors", 111L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 구역")
        void fail_NotFoundZone() throws Exception {
            given(zoneSensorService.getZoneSensors(111L))
                    .willThrow(new ZoneNotFoundException());

            mockMvc.perform(get("/api/core/zones/{zone-id}/zone-sensors", 111L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 센서 정보 수정 PUT /api/core/zones/{zone-id}/zone-sensors/{zone-sensor-id}")
    class updateZoneSensorInfo {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            ZoneSensorUpdateRequest request = new ZoneSensorUpdateRequest("수정된 이름", "수정된 설명");
            ZoneSensorDetailResponse response = new ZoneSensorDetailResponse(
                    1L, 11L, 111L,
                    "테스트 디바이스", "테스트 좆기", "테스트 저장소",
                    "테스트 구역", "수정된 이름", "수정된 설명");

            given(zoneSensorService.updateZoneSensorInfo(111L, 1L, request))
                    .willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(zoneSensorDetailResponseFields("data."));

            mockMvc.perform(put("/api/core/zones/{zone-id}/zone-sensors/{zone-sensor-id}", 111L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.deviceEui").value("테스트 디바이스"))
                    .andExpect(jsonPath("$.data.name").value("수정된 이름"))
                    .andExpect(jsonPath("$.data.description").value("수정된 설명"))
                    .andDo(document("zone-sensor-update",
                            pathParameters(
                                    parameterWithName("zone-id").description("저장소 ID"),
                                    parameterWithName("zone-sensor-id").description("센서 ID")
                            ),
                            requestFields(
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("등록할 센서 이름(필수)"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("등록할 센서 설명(선택)").optional()
                            ),
                            responseFields(
                                    responseFields
                            )));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            ZoneSensorUpdateRequest request = new ZoneSensorUpdateRequest("", "수정된 설명");

            mockMvc.perform(put("/api/core/zones/{zone-id}/zone-sensors/{zone-sensor-id}", 111L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            ZoneSensorUpdateRequest request = new ZoneSensorUpdateRequest("수정된 이름", "수정된 설명");

            given(zoneSensorService.updateZoneSensorInfo(111L, 1L, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(put("/api/core/zones/{zone-id}/zone-sensors/{zone-sensor-id}", 111L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 구역")
        void fail_NotFoundZone() throws Exception {
            ZoneSensorUpdateRequest request = new ZoneSensorUpdateRequest("수정된 이름", "수정된 설명");

            given(zoneSensorService.updateZoneSensorInfo(111L, 1L, request))
                    .willThrow(new ZoneNotFoundException());

            mockMvc.perform(put("/api/core/zones/{zone-id}/zone-sensors/{zone-sensor-id}", 111L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 구역 센서")
        void fail_NotFoundZoneSensor() throws Exception {
            ZoneSensorUpdateRequest request = new ZoneSensorUpdateRequest("수정된 이름", "수정된 설명");

            given(zoneSensorService.updateZoneSensorInfo(111L, 1L, request))
                    .willThrow(new ZoneSensorNotFoundException());

            mockMvc.perform(put("/api/core/zones/{zone-id}/zone-sensors/{zone-sensor-id}", 111L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 센서 삭제 DELETE /api/core/zones/{zone-id}/zone-sensors/{zone-sensor-id}")
    class deleteZoneSensor {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/zones/{zone-id}/zone-sensors/{zone-sensor-id}", 111L, 1L))
                    .andExpect(status().isNoContent())
                    .andDo(document("zone-sensor-delete",
                            pathParameters(
                                    parameterWithName("zone-id").description("저장소 ID"),
                                    parameterWithName("zone-sensor-id").description("센서 ID")
                            )));

            verify(zoneSensorService).deleteZoneSensor(111L, 1L);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(zoneSensorService)
                    .deleteZoneSensor(111L, 1L);

            mockMvc.perform(delete("/api/core/zones/{zone-id}/zone-sensors/{zone-sensor-id}", 111L, 1L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 구역")
        void fail_NotFoundZone() throws Exception {
            willThrow(new ZoneNotFoundException()).given(zoneSensorService)
                    .deleteZoneSensor(111L, 1L);

            mockMvc.perform(delete("/api/core/zones/{zone-id}/zone-sensors/{zone-sensor-id}", 111L, 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 구역 센서")
        void fail_NotFoundZoneSensor() throws Exception {
            willThrow(new ZoneSensorNotFoundException()).given(zoneSensorService)
                    .deleteZoneSensor(111L, 1L);

            mockMvc.perform(delete("/api/core/zones/{zone-id}/zone-sensors/{zone-sensor-id}", 111L, 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 센서 위치 정보 조회 GET /api/core/internal/devices/location")
    class getDeviceLocation {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            DeviceLocationResponse response = new DeviceLocationResponse(
                    1L, 11L, 111L, "테스트 디바이스");

            given(zoneSensorService.getDeviceLocation("테스트 디바이스"))
                    .willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(List.of(
                    fieldWithPath("data.organizationId").type(JsonFieldType.NUMBER).description("조직 ID"),
                    fieldWithPath("data.storageId").type(JsonFieldType.NUMBER).description("저장소 ID"),
                    fieldWithPath("data.zoneId").type(JsonFieldType.NUMBER).description("구역 ID"),
                    fieldWithPath("data.deviceEui").type(JsonFieldType.STRING).description("센서(디바이스) ID")
            ));

            mockMvc.perform(get("/api/core/internal/devices/location")
                            .param("device-eui", "테스트 디바이스"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.organizationId").value(1L))
                    .andExpect(jsonPath("$.data.storageId").value(11L))
                    .andExpect(jsonPath("$.data.zoneId").value(111L))
                    .andExpect(jsonPath("$.data.deviceEui").value("테스트 디바이스"))
                    .andDo(document("zone-sensor-get-location",
                            queryParameters(
                                    parameterWithName("device-eui").description("센서(디바이스) ID")
                            ),
                            responseFields(
                                    responseFields
                            )));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 구역 센서")
        void fail_NotFoundZoneSensor() throws Exception {
            given(zoneSensorService.getDeviceLocation("테스트 디바이스"))
                    .willThrow(new ZoneSensorNotFoundException());

            mockMvc.perform(get("/api/core/internal/devices/location")
                            .param("device-eui", "테스트 디바이스"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    private List<FieldDescriptor> zoneSensorInfoResponseFields(String prefix){
        return List.of(
                fieldWithPath(prefix + "zoneSensorId").type(JsonFieldType.NUMBER).description("센서 ID"),
                fieldWithPath(prefix + "zoneId").type(JsonFieldType.NUMBER).description("구역 ID"),
                fieldWithPath(prefix + "deviceEui").type(JsonFieldType.STRING).description("Device EUI"),
                fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("센서 이름")
        );
    }

    private List<FieldDescriptor> zoneSensorDetailResponseFields(String prefix){
        return List.of(
                fieldWithPath(prefix + "zoneSensorId").type(JsonFieldType.NUMBER).description("센서 ID"),
                fieldWithPath(prefix + "storageId").type(JsonFieldType.NUMBER).description("저장소 ID"),
                fieldWithPath(prefix + "zoneId").type(JsonFieldType.NUMBER).description("구역 ID"),
                fieldWithPath(prefix + "deviceEui").type(JsonFieldType.STRING).description("Device EUI"),
                fieldWithPath(prefix + "organizationName").type(JsonFieldType.STRING).description("상위 조직 이름"),
                fieldWithPath(prefix + "storageName").type(JsonFieldType.STRING).description("상위 저장소 이름"),
                fieldWithPath(prefix + "zoneName").type(JsonFieldType.STRING).description("상위 구역 이름"),
                fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("센서 이름"),
                fieldWithPath(prefix + "description").type(JsonFieldType.STRING).description("센서 설명").optional()
        );
    }
}