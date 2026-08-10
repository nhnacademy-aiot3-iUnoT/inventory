package com.nhnacademy.inventory.organizations.organization.controller;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgCreateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgSearchRequest;
import com.nhnacademy.inventory.organizations.organization.dto.response.AdminOrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgCreateResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgSearchResponse;
import com.nhnacademy.inventory.organizations.organization.exception.OrgNotFoundException;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationService;
import com.nhnacademy.inventory.support.RestDocsUtils;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(OrganizationAdminController.class)
class OrganizationAdminControllerTest extends SupportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @Nested
    @DisplayName("조직 생성 POST /api/core/admin/organizations")
    class createOrganization {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            OrgCreateRequest request = new OrgCreateRequest(
                    "1234567890",
                    "test@test.com",
                    "테스트 조직"
            );

            OrgCreateResponse response = new OrgCreateResponse(
                    1L,
                    "테스트 조직"
            );

            given(organizationService.createOrganization(request)).willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());

            responseFields.addAll(List.of(
                    fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("조직 ID"),
                    fieldWithPath("data.name").type(JsonFieldType.STRING).description("조직 이름")
            ));

            mockMvc.perform(post("/api/core/admin/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("테스트 조직"))

                    .andDo(document("organization-admin-create",
                            requestFields(
                                    fieldWithPath("businessNumber").type(JsonFieldType.STRING).description("사업자 번호"),
                                    fieldWithPath("email").type(JsonFieldType.STRING).description("Owner 이메일"),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("테스트 조직")
                            ),
                            responseFields(responseFields)
                    ));

            verify(organizationService).createOrganization(request);
        }


        @Test
        @DisplayName("실패 - 잘못된 입력")
        void invalidInput() throws Exception {
            OrgCreateRequest request = new OrgCreateRequest(
                    "", "invalid-email", ""
            );

            mockMvc.perform(post("/api/core/admin/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {

            OrgCreateRequest request = new OrgCreateRequest(
                    "1234567890",
                    "test@test.com",
                    "테스트 조직"
            );

            given(organizationService.createOrganization(request)).willThrow(new ForbiddenException());

            mockMvc.perform(post("/api/core/admin/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("조직 목록 조회 GET /api/core/admin/organizations")
    class getOrganizationList {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            OrgSearchResponse response = new OrgSearchResponse(
                    1L, "1234567890",
                    "테스트 조직", OrganizationStatus.ACTIVE, LocalDateTime.now()
            );

            Page<OrgSearchResponse> page = new PageImpl<>(List.of(response));

            given(organizationService.getOrganizationList(any(OrgSearchRequest.class), any(Pageable.class))).willReturn(page);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());

            responseFields.addAll(List.of(
                    fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("조직 ID"),
                    fieldWithPath("data.content[].businessNumber").type(JsonFieldType.STRING).description("사업자 번호"),
                    fieldWithPath("data.content[].name").type(JsonFieldType.STRING).description("조직 이름"),
                    fieldWithPath("data.content[].status").type(JsonFieldType.STRING).description("조직 상태"),
                    fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING).description("조직 생성 일시"),

                    fieldWithPath("data.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                    fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                    fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 데이터 개수"),
                    fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                    fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
            ));

            mockMvc.perform(get("/api/core/admin/organizations")
                            .param("name", "테스트")
                            .param("page", "0")
                            .param("size", "15")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].id").value(1))
                    .andExpect(jsonPath("$.data.content[0].businessNumber").value("1234567890"))
                    .andExpect(jsonPath("$.data.content[0].name").value("테스트 조직"))
                    .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.content[0].createdAt").exists())

                    .andDo(document("organization-admin-list",
                            queryParameters(
                                    parameterWithName("status").description("조직 상태").optional(),
                                    parameterWithName("name").description("조직 검색어").optional(),
                                    parameterWithName("page").description("페이지 번호").optional(),
                                    parameterWithName("size").description("페이지 크기").optional()
                            ),
                            responseFields(responseFields)
                    ));

            verify(organizationService).getOrganizationList(any(OrgSearchRequest.class), any(Pageable.class));
        }


        @Test
        @DisplayName("성공 - 조회 결과 없음")
        void success_empty() throws Exception {
            Page<OrgSearchResponse> page = new PageImpl<>(List.of());

            given(organizationService.getOrganizationList(any(OrgSearchRequest.class), any(Pageable.class))).willReturn(page);

            mockMvc.perform(get("/api/core/admin/organizations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(0));
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            given(organizationService.getOrganizationList(any(OrgSearchRequest.class), any(Pageable.class))).willThrow(new ForbiddenException());

            mockMvc.perform(get("/api/core/admin/organizations"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }


    @Nested
    @DisplayName("조직 상세 조회 GET /api/core/admin/organizations/{organization-id}")
    class getOrganizationInfo {

        @Test
        @DisplayName("성공")
        void success() throws Exception {

            AdminOrgDetailResponse response =
                    new AdminOrgDetailResponse(
                            1L, "1234567890", "테스트 조직",
                            "광주시 북구", "12345", "101호",
                            OrganizationStatus.ACTIVE, LocalDateTime.now()

                    );

            given(organizationService.getOrganizationForAdmin(1L)).willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());

            responseFields.addAll(List.of(
                    fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("조직 ID"),
                    fieldWithPath("data.businessNumber").type(JsonFieldType.STRING).description("사업자 번호"),
                    fieldWithPath("data.name").type(JsonFieldType.STRING).description("조직 이름"),
                    fieldWithPath("data.roadAddress").type(JsonFieldType.STRING).description("주소"),
                    fieldWithPath("data.zipCode").type(JsonFieldType.STRING).description("우편번호"),
                    fieldWithPath("data.addressDetail").type(JsonFieldType.STRING).description("상세 주소"),
                    fieldWithPath("data.status").type(JsonFieldType.STRING).description("조직 상태"),
                    fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 일시")
                )
            );

            mockMvc.perform(get("/api/core/admin/organizations/{organization-id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("테스트 조직"))

                    .andDo(document("organization-admin-get",
                            pathParameters(parameterWithName("organization-id").description("조직 ID")),

                            responseFields(responseFields)
                    ));

            verify(organizationService)
                    .getOrganizationForAdmin(1L);
        }


        @Test
        @DisplayName("실패 - 조직 없음")
        void notFound() throws Exception {

            given(organizationService.getOrganizationForAdmin(1L)).willThrow(new OrgNotFoundException());

            mockMvc.perform(get("/api/core/admin/organizations/{organization-id}", 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {

            given(organizationService.getOrganizationForAdmin(1L)).willThrow(new ForbiddenException());

            mockMvc.perform(get("/api/core/admin/organizations/{organization-id}", 1L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }


    @Nested
    @DisplayName("조직 삭제 DELETE /api/core/admin/organizations/{organization-id}")
    class deleteOrganization {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/admin/organizations/{organization-id}", 1L))
                    .andExpect(status().isNoContent())
                    .andDo(document("organization-admin-delete",
                            pathParameters(parameterWithName("organization-id").description("조직 ID"))
                    ));

            verify(organizationService).deleteOrganization(1L);
        }


        @Test
        @DisplayName("실패 - 조직 없음")
        void notFound() throws Exception {

            willThrow(new OrgNotFoundException()).given(organizationService).deleteOrganization(1L);

            mockMvc.perform(delete("/api/core/admin/organizations/{organization-id}", 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {

            willThrow(new ForbiddenException()).given(organizationService).deleteOrganization(1L);

            mockMvc.perform(delete("/api/core/admin/organizations/{organization-id}", 1L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
