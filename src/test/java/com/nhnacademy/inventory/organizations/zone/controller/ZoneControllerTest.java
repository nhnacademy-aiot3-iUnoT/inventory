package com.nhnacademy.inventory.organizations.zone.controller;

import com.nhnacademy.inventory.global.error.GlobalExceptionHandler;
import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import com.nhnacademy.inventory.organizations.zone.dto.*;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNameAlreadyExistsException;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNotFoundException;
import com.nhnacademy.inventory.organizations.zone.service.ZoneService;
import com.nhnacademy.inventory.support.RestDocsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ZoneController.class)
@Import(GlobalExceptionHandler.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class ZoneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ZoneService zoneService;

    private UUID accountUuid;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
               RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();

        accountUuid = UUID.randomUUID();
    }

    @Nested
    @DisplayName("구역 생성 POST /api/core/storages/{storageId}/zones")
    class createZone {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역", "설명");
            ZoneInfoResponse response = new ZoneInfoResponse(
                    1L, 11L,
                    "테스트 구역", "설명",
                    ZoneStatus.ACTIVE, EnvStatus.NORMAL,
                    LocalDateTime.now(), null
            );

            given(zoneService.createZone(111L, accountUuid, request)).willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(zoneInfoResponseFields("data."));

            mockMvc.perform(post("/api/core/storages/{storageId}/zones", 111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("테스트 구역"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andDo(document("zone-create",
                            requestHeaders(headerWithName("X-USER-ID").description("유저 UUID")),
                            pathParameters(
                                    parameterWithName("storageId").description("저장소 ID")
                            ),
                            requestFields(
                                    fieldWithPath("name").description("생성할 구역 이름(필수)"),
                                    fieldWithPath("description").description("생성할 구역 설명(선택)").optional()
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            ZoneCreateRequest request = new ZoneCreateRequest("", "설명");

            mockMvc.perform(post("/api/core/storages/{storageId}/zones", 111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역", "설명");

            given(zoneService.createZone(111L, accountUuid, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(post("/api/core/storages/{storageId}/zones", 111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역", "설명");

            given(zoneService.createZone(111L, accountUuid, request))
                    .willThrow(new StorageNotFoundException());

            mockMvc.perform(post("/api/core/storages/{storageId}/zones", 111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 중복 이름")
        void fail_DuplicationName() throws Exception {
            ZoneCreateRequest request = new ZoneCreateRequest("테스트 구역", "설명");

            given(zoneService.createZone(111L, accountUuid, request))
                    .willThrow(new ZoneNameAlreadyExistsException());

            mockMvc.perform(post("/api/core/storages/{storageId}/zones", 111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 목록 조회 GET /api/core/storages/{storageId}/zones")
    class getZones {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            ZoneInfoResponse response = new ZoneInfoResponse(
                    1L, 11L,
                    "테스트 구역", "설명",
                    ZoneStatus.ACTIVE, EnvStatus.NORMAL,
                    LocalDateTime.now(), null
            );
            List<ZoneInfoResponse> responseList = List.of(response);

            given(zoneService.getZones(111L, accountUuid))
                    .willReturn(responseList);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(zoneInfoResponseFields("data[]."));

            mockMvc.perform(get("/api/core/storages/{storageId}/zones", 111L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("테스트 구역"))
                    .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                    .andDo(document("zone-get-list",
                            requestHeaders(headerWithName("X-USER-ID").description("유저 UUID")),
                            pathParameters(
                                    parameterWithName("storageId").description("저장소 ID")
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("정상 처리 테스트(빈 배열)")
        void success_empty() throws Exception {
            given(zoneService.getZones(111L, accountUuid))
                    .willReturn(List.of());

            mockMvc.perform(get("/api/core/storages/{storageId}/zones", 111L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            given(zoneService.getZones(111L, accountUuid))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(get("/api/core/storages/{storageId}/zones", 111L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {

            given(zoneService.getZones(111L, accountUuid))
                    .willThrow(new StorageNotFoundException());

            mockMvc.perform(get("/api/core/storages/{storageId}/zones", 111L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 정보 업데이트 PUT /api/core/storages/{storageId}/zones{zoneId}")
    class updateZoneInfo {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");
            ZoneInfoResponse response = new ZoneInfoResponse(
                    1L, 11L,
                    "업데이트 구역", "업데이트 설명",
                    ZoneStatus.ACTIVE, EnvStatus.NORMAL,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(zoneService.updateZone(111L, 1111L, accountUuid, request)).willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(zoneInfoResponseFields("data."));

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("업데이트 구역"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andDo(document("zone-update",
                            requestHeaders(headerWithName("X-USER-ID").description("유저 UUID")),
                            pathParameters(
                                    parameterWithName("storageId").description("저장소 ID"),
                                    parameterWithName("zoneId").description("구역 ID")
                            ),
                            requestFields(
                                    fieldWithPath("name").description("수정할 구역 이름(필수)"),
                                    fieldWithPath("description").description("수정할 구역 설명(선택)").optional()
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            ZoneUpdateRequest request = new ZoneUpdateRequest("", "업데이트 설명");

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

            given(zoneService.updateZone(111L, 1111L, accountUuid, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

            given(zoneService.updateZone(111L, 1111L, accountUuid, request))
                    .willThrow(new StorageNotFoundException());

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 구역 없음")
        void fail_NotFoundZone() throws Exception {
            ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

            given(zoneService.updateZone(111L, 1111L, accountUuid, request))
                    .willThrow(new ZoneNotFoundException());

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 중복 이름")
        void fail_DuplicationName() throws Exception {
            ZoneUpdateRequest request = new ZoneUpdateRequest("업데이트 구역", "업데이트 설명");

            given(zoneService.updateZone(111L, 1111L, accountUuid, request))
                    .willThrow(new ZoneNameAlreadyExistsException());

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 상태 업데이트 PUT /api/core/storages/{storageId}/zones/{zoneId}/status")
    class updateZoneStatus {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(ZoneStatus.INACTIVE);
            ZoneInfoResponse response = new ZoneInfoResponse(
                    1L, 11L,
                    "테스트 구역", "테스트 설명",
                    ZoneStatus.INACTIVE, EnvStatus.NORMAL,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(zoneService.updateZoneStatus(111L, 1111L, accountUuid, request))
                    .willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(zoneInfoResponseFields("data."));

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}/status", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("테스트 구역"))
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                    .andDo(document("zone-update-status",
                            requestHeaders(headerWithName("X-USER-ID").description("유저 UUID")),
                            pathParameters(
                                    parameterWithName("storageId").description("저장소 ID"),
                                    parameterWithName("zoneId").description("구역 ID")
                            ),
                            requestFields(
                                    fieldWithPath("status").description("수정된 구역 상태")
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(null);

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(ZoneStatus.INACTIVE);

            given(zoneService.updateZoneStatus(111L, 1111L, accountUuid, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}/status", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(ZoneStatus.INACTIVE);

            given(zoneService.updateZoneStatus(111L, 1111L, accountUuid, request))
                    .willThrow(new StorageNotFoundException());

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}/status", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 구역 없음")
        void fail_NotFoundZone() throws Exception {
            ZoneStatusUpdateRequest request = new ZoneStatusUpdateRequest(ZoneStatus.INACTIVE);

            given(zoneService.updateZoneStatus(111L, 1111L, accountUuid, request))
                    .willThrow(new ZoneNotFoundException());

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}/status", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 환경 상태 업데이트 PUT /api/core/storages/{storageId}/zones/{zoneId}/env-status")
    class updateZoneEnvStatus {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(EnvStatus.CRITICAL);
            ZoneInfoResponse response = new ZoneInfoResponse(
                    1L, 11L,
                    "테스트 구역", "테스트 설명",
                    ZoneStatus.ACTIVE, EnvStatus.CRITICAL,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(zoneService.updateZoneEnvStatus(111L, 1111L, accountUuid, request))
                    .willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(zoneInfoResponseFields("data."));

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}/env-status", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("테스트 구역"))
                    .andExpect(jsonPath("$.data.envStatus").value("CRITICAL"))
                    .andDo(document("zone-update-env",
                            requestHeaders(headerWithName("X-USER-ID").description("유저 UUID")),
                            pathParameters(
                                    parameterWithName("storageId").description("저장소 ID"),
                                    parameterWithName("zoneId").description("구역 ID")
                            ),
                            requestFields(
                                    fieldWithPath("envStatus").description("수정된 구역 환경상태")
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(null);

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}/env-status", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(EnvStatus.CRITICAL);

            given(zoneService.updateZoneEnvStatus(111L, 1111L, accountUuid, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}/env-status", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(EnvStatus.CRITICAL);

            given(zoneService.updateZoneEnvStatus(111L, 1111L, accountUuid, request))
                    .willThrow(new StorageNotFoundException());

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}/env-status", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 구역 없음")
        void fail_NotFoundZone() throws Exception {
            ZoneEnvStatusUpdateRequest request = new ZoneEnvStatusUpdateRequest(EnvStatus.CRITICAL);

            given(zoneService.updateZoneEnvStatus(111L, 1111L, accountUuid, request))
                    .willThrow(new ZoneNotFoundException());

            mockMvc.perform(put("/api/core/storages/{storageId}/zones/{zoneId}/env-status", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 삭제 DELETE /api/core/storages/{storageId}/zones/{zoneId}")
    class deleteZone {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/storages/{storageId}/zones/{zoneId}", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isNoContent())
                    .andDo(document("zone-delete",
                            requestHeaders(headerWithName("X-USER-ID").description("유저 UUID")),
                            pathParameters(
                                    parameterWithName("storageId").description("저장소 ID"),
                                    parameterWithName("zoneId").description("구역 ID")
                            )
                    ));

            verify(zoneService).closeZone(111L, 1111L, accountUuid);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(zoneService)
                    .closeZone(111L, 1111L, accountUuid);

            mockMvc.perform(delete("/api/core/storages/{storageId}/zones/{zoneId}", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            willThrow(new StorageNotFoundException()).given(zoneService)
                    .closeZone(111L, 1111L, accountUuid);

            mockMvc.perform(delete("/api/core/storages/{storageId}/zones/{zoneId}", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 구역 없음")
        void fail_NotFoundZone() throws Exception {
            willThrow(new ZoneNotFoundException()).given(zoneService)
                    .closeZone(111L, 1111L, accountUuid);

            mockMvc.perform(delete("/api/core/storages/{storageId}/zones/{zoneId}", 111L, 1111L)
                            .header("X-USER-ID", accountUuid.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    private List<FieldDescriptor> zoneInfoResponseFields(String prefix){
        return List.of(
                fieldWithPath(prefix + "zoneId").type(JsonFieldType.NUMBER).description("구역 ID"),
                fieldWithPath(prefix + "storageId").type(JsonFieldType.NUMBER).description("저장소 ID"),
                fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("구역 이름"),
                fieldWithPath(prefix + "description").type(JsonFieldType.STRING).description("구역 설명").optional(),
                fieldWithPath(prefix + "status").type(JsonFieldType.STRING).description("구역 상태"),
                fieldWithPath(prefix + "envStatus").type(JsonFieldType.STRING).description("환경 상태"),
                fieldWithPath(prefix + "createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                fieldWithPath(prefix + "updatedAt").type(JsonFieldType.STRING).description("수정 일시").optional()
        );
    }
}