package com.nhnacademy.inventory.organizations.member.controller;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.dto.response.MemberOrganizationResponse;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
import com.nhnacademy.inventory.support.RestDocsUtils;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationMemberInternalController.class)
class OrganizationMemberInternalControllerTest extends SupportControllerTest {

    @MockitoBean
    private OrganizationMemberService orgMemberService;

    @Nested
    @DisplayName("조직 정보 조회 GET /api/core/internal/members/{account-uuid}/organization")
    class GetMemberOrganization {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID accountUuid = UUID.randomUUID();
            MemberOrganizationResponse response = new MemberOrganizationResponse(
                    accountUuid,
                    1L,
                    OrganizationRole.ORG_OWNER
            );

            given(orgMemberService.getMemberOrganization(accountUuid)).willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(List.of(
                    fieldWithPath("data.accountUuid").type(JsonFieldType.STRING).description("계정 UUID"),
                    fieldWithPath("data.organizationId").type(JsonFieldType.NUMBER).description("소속 조직 ID"),
                    fieldWithPath("data.organizationRole").type(JsonFieldType.STRING).description("조직 역할 (ORG_BOSS, ORG_OWNER, ORG_MEMBER)")
            ));

            mockMvc.perform(get("/api/core/internal/members/{account-uuid}/organization", accountUuid))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accountUuid").value(accountUuid.toString()))
                    .andExpect(jsonPath("$.data.organizationId").value(1L))
                    .andExpect(jsonPath("$.data.organizationRole").value("ORG_OWNER"))
                    .andDo(document("internal-get-member-organization",
                            pathParameters(parameterWithName("account-uuid").description("계정 UUID")),
                            responseFields(responseFields)
                    ));

            verify(orgMemberService).getMemberOrganization(accountUuid);
        }

        @Test
        @DisplayName("실패 - 소속된 조직이 없음")
        void notFound() throws Exception {
            UUID accountUuid = UUID.randomUUID();

            willThrow(new UserOrgNotFoundException()).given(orgMemberService).getMemberOrganization(accountUuid);

            mockMvc.perform(get("/api/core/internal/members/{account-uuid}/organization", accountUuid))
                    .andExpect(status().isNotFound());
        }
    }
}
