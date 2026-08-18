package com.nhnacademy.inventory.organizations.invitation.controller;

import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSignupRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.request.SignupCompensateRequest;
import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
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
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvitationInternalController.class)
class InvitationInternalControllerTest extends SupportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvitationService invitationService;

    @Nested
    @DisplayName("회원가입 초대 사용 POST /api/core/internal/invitations/use")
    class SignupWithInvitation {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID token = UUID.randomUUID();
            UUID accountUuid = UUID.randomUUID();

            InvitationSignupRequest request = new InvitationSignupRequest(
                    token,
                    "test@test.com",
                    accountUuid
            );

            mockMvc.perform(post("/api/core/internal/invitations/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isNoContent())
                    .andDo(document("invitation-signup",
                            requestFields(
                                    fieldWithPath("token").type(JsonFieldType.STRING).description("초대 토큰"),
                                    fieldWithPath("email").type(JsonFieldType.STRING).description("회원가입 이메일"),
                                    fieldWithPath("accountUuid").type(JsonFieldType.STRING).description("Account UUID")
                            )
                    ));

            verify(invitationService).signupWithInvitation(request);
        }
    }

    @Nested
    @DisplayName("회원가입 실패 보상 POST /api/core/internal/invitations/compensate")
    class CompensateSignup {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            UUID token = UUID.randomUUID();
            UUID accountUuid = UUID.randomUUID();

            SignupCompensateRequest request = new SignupCompensateRequest(token, accountUuid);

            mockMvc.perform(post("/api/core/internal/invitations/compensate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isNoContent())

                    .andDo(document("invitation-signup-compensate",
                            requestFields(
                                    fieldWithPath("token").type(JsonFieldType.STRING).description("초대 토큰"),
                                    fieldWithPath("accountUuid").type(JsonFieldType.STRING).description("Account UUID")
                            )
                    ));

            verify(invitationService).compensateSignup(request);
        }
    }
}
