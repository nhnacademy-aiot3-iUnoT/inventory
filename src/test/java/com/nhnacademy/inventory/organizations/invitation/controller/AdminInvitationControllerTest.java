package com.nhnacademy.inventory.organizations.invitation.controller;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.invitation.exception.InvitationNotFoundException;
import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminInvitationController.class)
class AdminInvitationControllerTest extends SupportControllerTest {

    @MockitoBean
    private InvitationService invitationService;

    @Nested
    @DisplayName("Owner 초대 재전송 POST /api/core/admin/organizations/{organization-id}/invitations/{invitation-id}/resend")
    class resendInvitation {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            mockMvc.perform(post("/api/core/admin/organizations/{organization-id}/invitations/{invitation-id}/resend", 1L, 10L))
                    .andExpect(status().isNoContent())
                    .andDo(document("admin-invitation-resend",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID"),
                                    parameterWithName("invitation-id").description("초대 ID")
                            )
                    ));

            verify(invitationService).resendInvitationForAdmin(1L, 10L);
        }

        @Test
        @DisplayName("실패 - 초대 없음")
        void notFound() throws Exception {
            willThrow(new InvitationNotFoundException()).given(invitationService).resendInvitationForAdmin(1L, 10L);

            mockMvc.perform(post("/api/core/admin/organizations/{organization-id}/invitations/{invitation-id}/resend", 1L, 10L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(invitationService).resendInvitationForAdmin(1L, 10L);

            mockMvc.perform(post("/api/core/admin/organizations/{organization-id}/invitations/{invitation-id}/resend", 1L, 10L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }


    @Nested
    @DisplayName("Owner 초대 취소 DELETE /api/core/admin/organizations/{organization-id}/invitations/{invitation-id}")
    class cancelInvitation {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/admin/organizations/{organization-id}/invitations/{invitation-id}", 1L, 10L))
                    .andExpect(status().isNoContent())
                    .andDo(document("admin-invitation-cancel",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID"),
                                    parameterWithName("invitation-id").description("초대 ID")
                            )
                    ));

            verify(invitationService).cancelInvitationForAdmin(1L, 10L);
        }

        @Test
        @DisplayName("실패 - 초대 없음")
        void notFound() throws Exception {
            willThrow(new InvitationNotFoundException()).given(invitationService).cancelInvitationForAdmin(1L, 10L);

            mockMvc.perform(delete("/api/core/admin/organizations/{organization-id}/invitations/{invitation-id}", 1L, 10L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(invitationService).cancelInvitationForAdmin(1L, 10L);

            mockMvc.perform(delete("/api/core/admin/organizations/{organization-id}/invitations/{invitation-id}", 1L, 10L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }


    @Nested
    @DisplayName("Owner 초대 재발급 POST /api/core/admin/organizations/{organization-id}/invitations/{invitation-id}/reissue")
    class reissueInvitation {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            mockMvc.perform(post("/api/core/admin/organizations/{organization-id}/invitations/{invitation-id}/reissue", 1L, 10L))
                    .andExpect(status().isNoContent())
                    .andDo(document("admin-invitation-reissue",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID"),
                                    parameterWithName("invitation-id").description("초대 ID")
                            )
                    ));

            verify(invitationService).reissueInvitationForAdmin(1L, 10L);
        }

        @Test
        @DisplayName("실패 - 초대 없음")
        void notFound() throws Exception {
            willThrow(new InvitationNotFoundException()).given(invitationService).reissueInvitationForAdmin(1L, 10L);

            mockMvc.perform(post("/api/core/admin/organizations/{organization-id}/invitations/{invitation-id}/reissue", 1L, 10L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(invitationService).reissueInvitationForAdmin(1L, 10L);

            mockMvc.perform(post("/api/core/admin/organizations/{organization-id}/invitations/{invitation-id}/reissue", 1L, 10L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
