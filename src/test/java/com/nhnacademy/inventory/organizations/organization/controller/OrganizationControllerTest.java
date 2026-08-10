package com.nhnacademy.inventory.organizations.organization.controller;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgUpdateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrganizationSetupRequest;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.exception.OrgNotFoundException;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(OrganizationController.class)
class OrganizationControllerTest extends SupportControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @Nested
    @DisplayName("조직 정보 조회 GET /api/core/organizations/me")
    class GetOrganizationInfo {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            OrgDetailResponse response = new OrgDetailResponse(
                    1L, "테스트 조직",
                    "광주시 남구", "12345",
                    "101호", "테스트 조직 설명",
                    OrganizationStatus.ACTIVE, LocalDateTime.now()
            );

            given(organizationService.getOrganizationForUser()).willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(List.of(
                    fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("조직 ID"),
                    fieldWithPath("data.name").type(JsonFieldType.STRING).description("조직 이름"),
                    fieldWithPath("data.roadAddress").type(JsonFieldType.STRING).description("도로명 주소"),
                    fieldWithPath("data.zipCode").type(JsonFieldType.STRING).description("우편번호"),
                    fieldWithPath("data.addressDetail").type(JsonFieldType.STRING).description("상세 주소"),
                    fieldWithPath("data.description").type(JsonFieldType.STRING).description("조직 설명").optional(),
                    fieldWithPath("data.status").type(JsonFieldType.STRING).description("조직 상태"),
                    fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("조직 생성 일시")
            ));

            mockMvc.perform(get("/api/core/organizations/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("테스트 조직"))
                    .andExpect(jsonPath("$.data.roadAddress").value("광주시 남구"))
                    .andExpect(jsonPath("$.data.zipCode").value("12345"))
                    .andExpect(jsonPath("$.data.addressDetail").value("101호"))
                    .andExpect(jsonPath("$.data.description").value("테스트 조직 설명"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.createdAt").exists())

                    .andDo(document("organization-get",
                            responseFields(responseFields)
                    ));

            verify(organizationService).getOrganizationForUser();
        }

        @Test
        @DisplayName("실패 - 조직 없음")
        void notFound() throws Exception {

            given(organizationService.getOrganizationForUser()).willThrow(new OrgNotFoundException());

            mockMvc.perform(
                            get("/api/core/organizations/me")
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {

            given(organizationService.getOrganizationForUser()).willThrow(new ForbiddenException());

            mockMvc.perform(
                            get("/api/core/organizations/me")
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }


    @Nested
    @DisplayName("조직 상태 변경 PUT /api/core/organizations/me/status")
    class updateOrganizationStatus {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            OrgStatusUpdateRequest request = new OrgStatusUpdateRequest(OrganizationStatus.INACTIVE);

            mockMvc.perform(
                            put("/api/core/organizations/me/status")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))

                    )
                    .andExpect(status().isNoContent())

                    .andDo(document("organization-update-status",
                            requestFields(
                                    fieldWithPath("status").type(JsonFieldType.STRING).description("변경하고자 하는 조직 상태")
                            )
                    ));

            verify(organizationService).updateOrganizationStatus(request);
        }


        @Test
        @DisplayName("실패 - 잘못된 입력")
        void invalidInput() throws Exception {
            OrgStatusUpdateRequest request = new OrgStatusUpdateRequest(null);

            mockMvc.perform(
                            put("/api/core/organizations/me/status")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            OrgStatusUpdateRequest request = new OrgStatusUpdateRequest(OrganizationStatus.INACTIVE);

            willThrow(new ForbiddenException()).given(organizationService).updateOrganizationStatus(request);

            mockMvc.perform(
                            put("/api/core/organizations/me/status")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }


        @Test
        @DisplayName("실패 - 조직 없음")
        void notFound() throws Exception {
            OrgStatusUpdateRequest request = new OrgStatusUpdateRequest(OrganizationStatus.INACTIVE);

            willThrow(new OrgNotFoundException()).given(organizationService).updateOrganizationStatus(request);

            mockMvc.perform(
                            put("/api/core/organizations/me/status")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("조직 정보 수정 PUT /api/core/organizations/me")
    class updateOrganization {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            OrgUpdateRequest request = new OrgUpdateRequest(
                            "광주광역시 남구", "12345",
                            "101호", "테스트 조직 설명"
            );

            mockMvc.perform(put("/api/core/organizations/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isNoContent())
                    .andDo(document("organization-update",
                            requestFields(fieldWithPath("roadAddress").type(JsonFieldType.STRING).description("조직 도로명 주소"),
                                    fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                                    fieldWithPath("addressDetail").type(JsonFieldType.STRING).description("상세 주소"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("조직 설명").optional()
                            )
                    ));
            verify(organizationService).updateOrganization(request);
        }


        @Test
        @DisplayName("실패 - 잘못된 입력")
        void invalidInput() throws Exception {
            OrgUpdateRequest request = new OrgUpdateRequest(
                            "", "12345",
                            "101호", "테스트 조직 설명"
            );

            mockMvc.perform(put("/api/core/organizations/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            OrgUpdateRequest request = new OrgUpdateRequest(
                            "광주광역시 남구", "12345",
                            "101호", "테스트 조직 설명"
            );

            willThrow(new ForbiddenException()).given(organizationService).updateOrganization(request);

            mockMvc.perform(put("/api/core/organizations/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }


        @Test
        @DisplayName("실패 - 조직 없음")
        void notFound() throws Exception {
            OrgUpdateRequest request = new OrgUpdateRequest(
                            "광주광역시 남구", "12345",
                            "101호", "테스트 조직 설명"
            );

            willThrow(new OrgNotFoundException()).given(organizationService).updateOrganization(request);

            mockMvc.perform(put("/api/core/organizations/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }


    @Nested
    @DisplayName("조직 초기화 PUT /api/core/organizations/me/setup")
    class setupOrganization {

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            OrganizationSetupRequest request = new OrganizationSetupRequest(
                             "12345","광주광역시 남구",
                            "101호", "테스트 조직 설명"
            );

            mockMvc.perform(put("/api/core/organizations/me/setup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isNoContent())

                    .andDo(document("organization-setup",
                            requestFields(fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                                    fieldWithPath("roadAddress").type(JsonFieldType.STRING).description("조직 도로명 주소"),
                                    fieldWithPath("addressDetail").type(JsonFieldType.STRING).description("상세 주소"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("조직 설명").optional()
                            )
                    ));

            verify(organizationService).setupOrganization(request);
        }


        @Test
        @DisplayName("실패 - 잘못된 입력")
        void invalidInput() throws Exception {
            OrganizationSetupRequest request = new OrganizationSetupRequest(
                            "", "12345",
                            "101호", "테스트 조직 설명"
            );

            mockMvc.perform(put("/api/core/organizations/me/setup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            OrganizationSetupRequest request = new OrganizationSetupRequest(
                             "12345","광주광역시 남구",
                            "101호", "테스트 조직 설명"
            );

            willThrow(new ForbiddenException())
                    .given(organizationService)
                    .setupOrganization(request);

            mockMvc.perform(
                            put("/api/core/organizations/me/setup")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }


        @Test
        @DisplayName("실패 - 조직 없음")
        void notFound() throws Exception {
            OrganizationSetupRequest request = new OrganizationSetupRequest(
                             "12345","광주광역시 남구",
                            "101호", "테스트 조직 설명"
            );

            willThrow(new OrgNotFoundException()).given(organizationService).setupOrganization(request);

            mockMvc.perform(put("/api/core/organizations/me/setup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
