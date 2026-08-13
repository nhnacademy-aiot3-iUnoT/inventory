package com.nhnacademy.inventory.organizations.storage.controller;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.storage.dto.StorageCreateRequest;
import com.nhnacademy.inventory.organizations.storage.dto.StorageInfoResponse;
import com.nhnacademy.inventory.organizations.storage.dto.StorageStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.storage.dto.StorageUpdateRequest;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNameAlreadyExistsException;
import com.nhnacademy.inventory.organizations.storage.exception.StorageNotFoundException;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
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

import java.time.LocalDateTime;
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

@WebMvcTest(StorageController.class)
class StorageControllerTest extends SupportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StorageService storageService;

    @Nested
    @DisplayName("구역 생성 POST /api/core/organizations/{organization-id}/storages")
    class createStorage {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            StorageCreateRequest request = new StorageCreateRequest("테스트 저장소" , "테스트 설명");
            StorageInfoResponse response = new StorageInfoResponse(
                    1L, 11L,
                    "테스트 저장소", "테스트 설명", StorageStatus.ACTIVE,
                    LocalDateTime.now(), null
            );

            given(storageService.createStorage(11L, request)).willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(storageInfoResponseFields("data."));

            mockMvc.perform(post("/api/core/organizations/{organization-id}/storages", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("테스트 저장소"))
                    .andExpect(jsonPath("$.data.description").value("테스트 설명"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andDo(document("storage-create",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID")
                            ),
                            requestFields(
                                    fieldWithPath("name").description("생성할 저장소 이름(필수)"),
                                    fieldWithPath("description").description("생성할 저장소 설명(선택)").optional()
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            StorageCreateRequest request = new StorageCreateRequest("" , "테스트 설명");

            mockMvc.perform(post("/api/core/organizations/{organization-id}/storages", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            StorageCreateRequest request = new StorageCreateRequest("테스트 저장소" , "테스트 설명");

            given(storageService.createStorage(11L, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(post("/api/core/organizations/{organization-id}/storages", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 중복 이름")
        void fail_DuplicationName() throws Exception {
            StorageCreateRequest request = new StorageCreateRequest("테스트 저장소" , "테스트 설명");

            given(storageService.createStorage(11L, request))
                    .willThrow(new StorageNameAlreadyExistsException());

            mockMvc.perform(post("/api/core/organizations/{organization-id}/storages", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 조회 GET /api/core/organizations/{organization-id}/storages")
    class getStorages {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            StorageInfoResponse response = new StorageInfoResponse(
                    1L, 11L,
                    "테스트 저장소", "테스트 설명", StorageStatus.ACTIVE,
                    LocalDateTime.now(), null
            );

            given(storageService.getStorages(11L))
                    .willReturn(List.of(response));

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(storageInfoResponseFields("data[]."));

            mockMvc.perform(get("/api/core/organizations/{organization-id}/storages", 11L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("테스트 저장소"))
                    .andExpect(jsonPath("$.data[0].description").value("테스트 설명"))
                    .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                    .andDo(document("storage-get-list",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID")
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("정상 처리 테스트(빈 배열)")
        void success_empty() throws Exception {
            given(storageService.getStorages(11L))
                    .willReturn(List.of());

            mockMvc.perform(get("/api/core/organizations/{organization-id}/storages", 11L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            given(storageService.getStorages(11L))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(get("/api/core/organizations/{organization-id}/storages", 11L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 수정 PUT /api/core/organizations/{organization-id}/storages/{storage-id}")
    class updateStorage {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            StorageUpdateRequest request = new StorageUpdateRequest("수정된 저장소", "수정된 설명");
            StorageInfoResponse response = new StorageInfoResponse(
                    1L, 11L,
                    "수정된 저장소", "수정된 설명", StorageStatus.ACTIVE,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(storageService.updateStorage(11L, 1L, request))
                    .willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(storageInfoResponseFields("data."));

            mockMvc.perform(put("/api/core/organizations/{organization-id}/storages/{storage-id}", 11L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("수정된 저장소"))
                    .andExpect(jsonPath("$.data.description").value("수정된 설명"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andDo(document("storage-update",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID"),
                                    parameterWithName("storage-id").description("저장소 ID")
                            ),
                            requestFields(
                                    fieldWithPath("name").description("수정할 저장소 이름(필수)"),
                                    fieldWithPath("description").description("수정할 저장소 설명(선택)").optional()
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            StorageUpdateRequest request = new StorageUpdateRequest("", "수정된 설명");

            mockMvc.perform(put("/api/core/organizations/{organization-id}/storages/{storage-id}", 11L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            StorageUpdateRequest request = new StorageUpdateRequest("수정된 저장소", "수정된 설명");

            given(storageService.updateStorage(11L, 1L, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(put("/api/core/organizations/{organization-id}/storages/{storage-id}", 11L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            StorageUpdateRequest request = new StorageUpdateRequest("수정된 저장소", "수정된 설명");

            given(storageService.updateStorage(11L, 1L, request))
                    .willThrow(new StorageNotFoundException());

            mockMvc.perform(put("/api/core/organizations/{organization-id}/storages/{storage-id}", 11L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 중복 이름")
        void fail_DuplicationName() throws Exception {
            StorageUpdateRequest request = new StorageUpdateRequest("수정된 저장소", "수정된 설명");

            given(storageService.updateStorage(11L, 1L, request))
                    .willThrow(new StorageNameAlreadyExistsException());

            mockMvc.perform(put("/api/core/organizations/{organization-id}/storages/{storage-id}", 11L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 상태 수정 PUT /api/core/organizations/{organization-id}/storages/{storage-id}/status")
    class updateStorageStatus {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            StorageStatusUpdateRequest request = new StorageStatusUpdateRequest(StorageStatus.INACTIVE);
            StorageInfoResponse response = new StorageInfoResponse(
                    1L, 11L,
                    "테스트 저장소", "테스트 설명", StorageStatus.INACTIVE,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            given(storageService.updateStorageStatus(11L, 1L, request))
                    .willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(storageInfoResponseFields("data."));

            mockMvc.perform(put("/api/core/organizations/{organization-id}/storages/{storage-id}/status", 11L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("테스트 저장소"))
                    .andExpect(jsonPath("$.data.description").value("테스트 설명"))
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                    .andDo(document("storage-update-status",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID"),
                                    parameterWithName("storage-id").description("저장소 ID")
                            ),
                            requestFields(
                                    fieldWithPath("status").description("수정된 저장소 상태")
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            StorageStatusUpdateRequest request = new StorageStatusUpdateRequest(null);

            mockMvc.perform(put("/api/core/organizations/{organization-id}/storages/{storage-id}/status", 11L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            StorageStatusUpdateRequest request = new StorageStatusUpdateRequest(StorageStatus.INACTIVE);

            given(storageService.updateStorageStatus(11L, 1L, request))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(put("/api/core/organizations/{organization-id}/storages/{storage-id}/status", 11L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            StorageStatusUpdateRequest request = new StorageStatusUpdateRequest(StorageStatus.INACTIVE);

            given(storageService.updateStorageStatus(11L, 1L, request))
                    .willThrow(new StorageNotFoundException());

            mockMvc.perform(put("/api/core/organizations/{organization-id}/storages/{storage-id}/status", 11L, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("구역 삭제 DELETE /api/core/organizations/{organization-id}/storages/{storage-id}")
    class deleteStorage {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/organizations/{organization-id}/storages/{storage-id}", 11L, 1L))
                    .andExpect(status().isNoContent())
                    .andDo(document("storage-delete",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID"),
                                    parameterWithName("storage-id").description("저장소 ID")
                            )
                    ));

            verify(storageService).closeStorage(11L, 1L);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(storageService)
                    .closeStorage(11L, 1L);

            mockMvc.perform(delete("/api/core/organizations/{organization-id}/storages/{storage-id}", 11L, 1L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 저장소 없음")
        void fail_NotFoundStorage() throws Exception {
            willThrow(new StorageNotFoundException()).given(storageService)
                    .closeStorage(11L, 1L);

            mockMvc.perform(delete("/api/core/organizations/{organization-id}/storages/{storage-id}", 11L, 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    private List<FieldDescriptor> storageInfoResponseFields(String prefix){
        return List.of(
                fieldWithPath(prefix + "storageId").type(JsonFieldType.NUMBER).description("저장소 ID"),
                fieldWithPath(prefix + "organizationId").type(JsonFieldType.NUMBER).description("조직 ID"),
                fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("저장소 이름"),
                fieldWithPath(prefix + "description").type(JsonFieldType.STRING).description("저장소 설명").optional(),
                fieldWithPath(prefix + "status").type(JsonFieldType.STRING).description("저장소 상태"),
                fieldWithPath(prefix + "createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                fieldWithPath(prefix + "updatedAt").type(JsonFieldType.STRING).description("수정 일시").optional()
        );
    }
}