package com.nhnacademy.inventory.organizations.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.dto.request.OrganizationMemberSearchRequest;
import com.nhnacademy.inventory.organizations.member.dto.request.OrganizationRoleUpdateRequest;
import com.nhnacademy.inventory.organizations.member.dto.response.OrganizationMemberResponse;
import com.nhnacademy.inventory.organizations.member.exception.OrgMemberNotFoundException;
import com.nhnacademy.inventory.organizations.member.exception.OrgMemberRoleChangeNotAllowedException;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationMemberController.class)
class OrganizationMemberControllerTest extends SupportControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationMemberService orgMemberService;

    @Nested
    @DisplayName("부서 지정 조직원 목록 조회 GET /api/core/members")
    class GetMembers {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            OrganizationMemberResponse response = new OrganizationMemberResponse(
                    1L,
                    "test@email.com",
                    OrganizationRole.ORG_OWNER,
                    LocalDateTime.now()
            );

            Page<OrganizationMemberResponse> page = new PageImpl<>(List.of(response));

            given(orgMemberService.findMembers(any(OrganizationMemberSearchRequest.class), any(Boolean.class), any(Pageable.class))).willReturn(page);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());

            responseFields.addAll(List.of(
                    fieldWithPath("data.content[].memberId").type(JsonFieldType.NUMBER).description("조직원 ID"),
                    fieldWithPath("data.content[].email").type(JsonFieldType.STRING).description("조직원 이메일"),
                    fieldWithPath("data.content[].role").type(JsonFieldType.STRING).description("조직원 Role"),
                    fieldWithPath("data.content[].joinedAt").type(JsonFieldType.STRING).description("조직원 가입 날짜"),

                    fieldWithPath("data.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                    fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                    fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 데이터 개수"),
                    fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                    fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
            ));

            mockMvc.perform(get("/api/core/members")
                    .param("page", "0")
                    .param("size", "10")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].email").value("test@email.com"))
            .andExpect(jsonPath("$.data.content[0].role").value("ORG_OWNER"))
            .andDo(document("orgMember-list",
                    queryParameters(
                            parameterWithName("email").description("조직원 이메일").optional(),
                            parameterWithName("role").description("조직원 Role").optional(),
                            parameterWithName("page").description("페이지 번호").optional(),
                            parameterWithName("size").description("페이지 크기").optional()
                    ),
                    responseFields(responseFields)
            ));

            verify(orgMemberService).findMembers(any(OrganizationMemberSearchRequest.class), any(Boolean.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("부서 미지정 조직원 목록 조회 GET /api/core/members/without-department")
    class GetMembersWithoutDepartment {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            OrganizationMemberResponse response = new OrganizationMemberResponse(
                            1L,
                            "test@email.com",
                            OrganizationRole.ORG_OWNER,
                            LocalDateTime.now()
                    );

            Page<OrganizationMemberResponse> page = new PageImpl<>(List.of(response));

            given(orgMemberService.findMembers(any(OrganizationMemberSearchRequest.class), any(Boolean.class), any(Pageable.class))).willReturn(page);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());

            responseFields.addAll(List.of(
                    fieldWithPath("data.content[].memberId").type(JsonFieldType.NUMBER).description("조직원 ID"),
                    fieldWithPath("data.content[].email").type(JsonFieldType.STRING).description("조직원 이메일"),
                    fieldWithPath("data.content[].role").type(JsonFieldType.STRING).description("조직원 Role"),
                    fieldWithPath("data.content[].joinedAt").type(JsonFieldType.STRING).description("조직원 가입 날짜"),

                    fieldWithPath("data.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                    fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                    fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 데이터 개수"),
                    fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                    fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
            ));

            mockMvc.perform(get("/api/core/members/without-department")
                            .param("page", "0")
                            .param("size", "10")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].memberId").value(1))
                    .andExpect(jsonPath("$.data.content[0].email").value("test@email.com"))
                    .andExpect(jsonPath("$.data.content[0].role").value("ORG_OWNER"))
                    .andDo(document("orgMember-without-department-list",
                            queryParameters(
                                    parameterWithName("email").description("조직원 이메일").optional(),
                                    parameterWithName("page").description("페이지 번호").optional(),
                                    parameterWithName("size").description("페이지 크기").optional()
                            ),
                            responseFields(responseFields)
                    ));

            verify(orgMemberService).findMembers(any(OrganizationMemberSearchRequest.class), any(Boolean.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("조직원 역할 수정 PUT /api/core/members/{member-id}/role")
    class UpdateRole {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            Long memberId = 1L;
            OrganizationRoleUpdateRequest roleUpdateRequest = new OrganizationRoleUpdateRequest(OrganizationRole.ORG_OWNER);

            mockMvc.perform(put("/api/core/members/{member-id}/role", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(roleUpdateRequest)))
                    .andExpect(status().isNoContent())
                    .andDo(document("member-role-change",
                            pathParameters(parameterWithName("member-id").description("조직원 ID"))
                            ));

            verify(orgMemberService).updateRole(memberId, roleUpdateRequest);
        }

        @Test
        @DisplayName("실패 - BOSS 아님")
        void forbidden() throws Exception {
            Long memberId = 1L;
            OrganizationRoleUpdateRequest roleUpdateRequest = new OrganizationRoleUpdateRequest(OrganizationRole.ORG_OWNER);

            willThrow(new ForbiddenException()).given(orgMemberService).updateRole(memberId, roleUpdateRequest);

            mockMvc.perform(put("/api/core/members/{member-id}/role", memberId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(roleUpdateRequest))
                    )
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - BOSS로 변경 시도")
        void bad_request() throws Exception {
            Long memberId = 1L;
            OrganizationRoleUpdateRequest roleUpdateRequest = new OrganizationRoleUpdateRequest(OrganizationRole.ORG_BOSS);

            willThrow(new OrgMemberRoleChangeNotAllowedException()).given(orgMemberService).updateRole(memberId, roleUpdateRequest);

            mockMvc.perform(put("/api/core/members/{member-id}/role", memberId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(roleUpdateRequest))
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("조직원 삭제 DELETE /api/core/members/{member-id}")
    class DeleteMember {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            Long memberId = 1L;

            mockMvc.perform(delete("/api/core/members/{member-id}", memberId))
                    .andExpect(status().isNoContent())
                    .andDo(document("member-delete",
                            pathParameters(parameterWithName("member-id").description("조직원 ID"))
                    ));

            verify(orgMemberService).deleteMember(memberId);
        }

        @Test
        @DisplayName("실패 - BOSS 아님")
        void forbidden() throws Exception {
            Long memberId = 1L;
            willThrow(new ForbiddenException()).given(orgMemberService).deleteMember(memberId);

            mockMvc.perform(delete("/api/core/members/{member-id}", memberId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 조직원이 아님")
        void notFound() throws Exception {
            Long memberId = 1L;
            willThrow(new OrgMemberNotFoundException()).given(orgMemberService).deleteMember(memberId);

            mockMvc.perform(delete("/api/core/members/{member-id}", memberId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("조직원 탈퇴 DELETE /api/core/members/me")
    class LeaveOrganization {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/members/me"))
                    .andExpect(status().isNoContent())
                    .andDo(document("leave-organization"));

            verify(orgMemberService).leaveOrganization();
        }

        @Test
        @DisplayName("실패 - 조직원이 아님")
        void notFound() throws Exception {
            willThrow(new UserOrgNotFoundException()).given(orgMemberService).leaveOrganization();
            mockMvc.perform(delete("/api/core/members/me"))
                    .andExpect(status().isNotFound());
        }
    }
}
