package com.nhnacademy.inventory.organizations.invitation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    @DisplayName("초대 메일 발송 성공")
    void sendInvitation_success() {
        String email = "test@test.com";
        String link = "https://iunot.cloud/signup/invite?token=1234";

        mailService.sendInvitation(email, link);

        // SimpleMailMessage 타입의 인자가 들어오면 나중에 저장할 수 있는 캡처 도구를 만든다.
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture()); // send() 호출 시 전달된 인자를 captor에 저장

        SimpleMailMessage message = captor.getValue();

        assertAll(
                () -> assertEquals("noreply@iunot.cloud", message.getFrom()),
                () -> assertEquals(email, message.getTo()[0]),
                () -> assertEquals("조직 초대 안내", message.getSubject()),
                () -> assertTrue(message.getText().contains(link))
        );
    }

    @Test
    @DisplayName("메일 발송 실패 - 예외 전파")
    void sendInvitation_fail() {
        String email = "test@test.com";
        String link = "https://test.com/invite";

        doThrow(new RuntimeException())
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class, () -> mailService.sendInvitation(email, link));
    }
}
