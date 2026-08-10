package com.nhnacademy.inventory.organizations.invitation.event;

import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
import com.nhnacademy.inventory.organizations.invitation.service.MailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationMailEventListenerTest {

    @Mock
    private MailService mailService;

    @Mock
    private InvitationService invitationService;

    @InjectMocks
    private InvitationMailEventListener listener;

    @Test
    @DisplayName("메일 전송 성공 시 이메일 발송 완료 처리")
    void handle_success() {
        UUID token = UUID.randomUUID();
        String email = "test@test.com";
        String link = "https://iunot.cloud/signup/invite?token=" + token;

        InvitationMailSendEvent event = new InvitationMailSendEvent(email, token);

        given(invitationService.createInvitationUrl(token)).willReturn(link);

        listener.handle(event);

        verify(mailService).sendInvitation(email, link);
        verify(invitationService).markEmailSent(token);
    }

    @Test
    @DisplayName("메일 전송 실패 시 이메일 발송 완료 처리 x")
    void handle_mailFail() {
        UUID token = UUID.randomUUID();
        String email = "test@test.com";

        InvitationMailSendEvent event = new InvitationMailSendEvent(email, token);

        given(invitationService.createInvitationUrl(token)).willReturn("link");

        doThrow(new RuntimeException())
                .when(mailService)
                .sendInvitation(email, "link");

        listener.handle(event);

        verify(mailService).sendInvitation(email, "link");
        verify(invitationService, never()).markEmailSent(any());
    }
}
