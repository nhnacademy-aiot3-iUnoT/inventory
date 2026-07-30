package com.nhnacademy.inventory.organizations.invitation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendInvitation(String email, String link){

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("조직 초대 안내");
        message.setText("아래 링크를 통해 가입해주세요.\n" + link);

        mailSender.send(message);
    }
}
