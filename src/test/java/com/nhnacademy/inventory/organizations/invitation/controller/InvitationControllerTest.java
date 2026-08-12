package com.nhnacademy.inventory.organizations.invitation.controller;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationCreateRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSearchRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationCreateResponse;
import com.nhnacademy.inventory.organizations.invitation.dto.response.InvitationSearchResponse;
import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
import com.nhnacademy.inventory.organizations.invitation.service.OrganizationInvitationService;
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
import java.util.UUID;

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

@WebMvcTest(InvitationController.class)
class InvitationControllerTest extends SupportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvitationService invitationService;

    @MockitoBean
    private OrganizationInvitationService organizationInvitationService;

    @Nested
    @DisplayName("조직원 초대 생성 POST /api/core/organizations/me/invitations")
    class CreateInvitation {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            InvitationCreateRequest request = new InvitationCreateRequest("test@test.com");

            InvitationCreateResponse response = new InvitationCreateResponse(1L, "test@test.com");

            given(organizationInvitationService.inviteMember(request)).willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());

            responseFields.addAll(List.of(
                    fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("초대 아이디"),
                    fieldWithPath("data.email").type(JsonFieldType.STRING).description("초대 대상 이메일")
            ));

            mockMvc.perform(post("/api/core/organizations/me/invitations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.email").value("test@test.com"))

                    .andDo(document("invitation-create",
                            requestFields(
                                    fieldWithPath("email").type(JsonFieldType.STRING).description("초대 대상 이메일")
                            ),
                            responseFields(responseFields)
                    ));

            verify(organizationInvitationService).inviteMember(request);
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            InvitationCreateRequest request = new InvitationCreateRequest("test@test.com");

            willThrow(new ForbiddenException()).given(organizationInvitationService).inviteMember(request);

            mockMvc.perform(post("/api/core/organizations/me/invitations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }


    @Nested
    @DisplayName("초대 목록 조회 GET /api/core/organizations/me/invitations")
    class GetInvitations {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            LocalDateTime createdAt = LocalDateTime.now();
            InvitationSearchResponse response = new InvitationSearchResponse(
                    1L,
                    "test@test.com",
                    InvitationStatus.ACTIVE,
                    false,
                    createdAt,
                    createdAt.plusDays(1)
            );

            Page<InvitationSearchResponse> page = new PageImpl<>(List.of(response));

            given(invitationService.getInvitations(any(InvitationSearchRequest.class), any(Pageable.class))).willReturn(page);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());

            responseFields.addAll(List.of(
                    fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("초대 ID"),
                    fieldWithPath("data.content[].email").type(JsonFieldType.STRING).description("초대 대상 이메일"),
                    fieldWithPath("data.content[].status").type(JsonFieldType.STRING).description("초대 상태"),
                    fieldWithPath("data.content[].reissued").type(JsonFieldType.BOOLEAN).description("재발급 여부"),
                    fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING).description("초대 생성 일시"),
                    fieldWithPath("data.content[].expiredAt").type(JsonFieldType.STRING).description("초대 토큰 만료 예정 일시"),

                    fieldWithPath("data.page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                    fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                    fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 데이터 개수"),
                    fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                    fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
            ));

            mockMvc.perform(get("/api/core/organizations/me/invitations")
                            .param("page", "0")
                            .param("size", "15")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].email").value("test@test.com"))
                    .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"))

                    .andDo(document("invitation-list",
                            queryParameters(
                                    parameterWithName("email").description("초대 대상 이메일").optional(),
                                    parameterWithName("status").description("초대 상태").optional(),
                                    parameterWithName("page").description("페이지 번호").optional(),
                                    parameterWithName("size").description("페이지 크기").optional()
                            ),
                            responseFields(responseFields)
                    ));

            verify(invitationService).getInvitations(any(InvitationSearchRequest.class), any(Pageable.class));
        }


        @Test
        @DisplayName("성공 - 조회 결과 없음")
        void empty() throws Exception {
            Page<InvitationSearchResponse> page = new PageImpl<>(List.of());

            given(invitationService.getInvitations(any(InvitationSearchRequest.class), any(Pageable.class))).willReturn(page);

            mockMvc.perform(get("/api/core/organizations/me/invitations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            given(invitationService.getInvitations(any(InvitationSearchRequest.class), any(Pageable.class))).willThrow(new ForbiddenException());

            mockMvc.perform(get("/api/core/organizations/me/invitations"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("초대 토큰 검증 GET /api/core/invitations/{token}")
    class ValidateInvitationToken {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID token = UUID.randomUUID();

            mockMvc.perform(get("/api/core/invitations/{token}", token))
                    .andExpect(status().isNoContent())
                    .andDo(document("invitation-validate-token",
                            pathParameters(parameterWithName("token").description("초대 토큰"))
                    ));

            verify(invitationService).validateInvitationToken(token);
        }
    }

    @Nested
    @DisplayName("초대 재전송 POST /api/core/organizations/me/invitations/{invitation-id}/resend")
    class ResendInvitation {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            Long invitationId = 1L;

            mockMvc.perform(post("/api/core/organizations/me/invitations/{invitation-id}/resend", invitationId))
                    .andExpect(status().isNoContent())
                    .andDo(document("invitation-resend",
                            pathParameters(parameterWithName("invitation-id").description("초대 ID"))
                    ));

            verify(invitationService).resendInvitation(invitationId);
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            Long invitationId = 1L;

            willThrow(new ForbiddenException()).given(invitationService).resendInvitation(invitationId);

            mockMvc.perform(post("/api/core/organizations/me/invitations/{invitation-id}/resend", invitationId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }


    @Nested
    @DisplayName("초대 취소 DELETE /api/core/organizations/me/invitations/{invitation-id}")
    class CancelInvitation {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            Long invitationId = 1L;

            mockMvc.perform(delete("/api/core/organizations/me/invitations/{invitation-id}", invitationId))
                    .andExpect(status().isNoContent())

                    .andDo(document("invitation-cancel",
                            pathParameters(parameterWithName("invitation-id").description("초대 ID"))
                    ));

            verify(invitationService).cancelInvitation(invitationId);
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            Long invitationId = 1L;

            willThrow(new ForbiddenException()).given(invitationService).cancelInvitation(invitationId);

            mockMvc.perform(delete("/api/core/organizations/me/invitations/{invitation-id}", invitationId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }


    @Nested
    @DisplayName("초대 재발급 POST /api/core/organizations/me/invitations/{invitation-id}/reissue")
    class ReissueInvitation {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            Long invitationId = 1L;

            InvitationCreateResponse response = new InvitationCreateResponse(1L, "test@test.com");

            given(invitationService.reissueInvitation(invitationId)).willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());

            responseFields.addAll(List.of(
                    fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("초대 아이디"),
                    fieldWithPath("data.email").type(JsonFieldType.STRING).description("초대 대상 이메일")
            ));

            mockMvc.perform(post("/api/core/organizations/me/invitations/{invitation-id}/reissue", invitationId))                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("test@test.com"))

                    .andDo(document("invitation-reissue",
                            pathParameters(parameterWithName("invitation-id").description("초대 ID")),
                            responseFields(responseFields)
                    ));

            verify(invitationService).reissueInvitation(invitationId);
        }


        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
           Long invitationId = 1L;

            willThrow(new ForbiddenException()).given(invitationService).reissueInvitation(invitationId);

            mockMvc.perform(post("/api/core/organizations/me/invitations/{invitation-id}/reissue", invitationId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
