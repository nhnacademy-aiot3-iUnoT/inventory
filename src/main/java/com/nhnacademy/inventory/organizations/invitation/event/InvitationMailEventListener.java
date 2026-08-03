package com.nhnacademy.inventory.organizations.invitation.event;

import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
import com.nhnacademy.inventory.organizations.invitation.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationMailEventListener {
    private final MailService mailService;
    private final InvitationService invitationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OwnerInvitationCreatedEvent event) {
        String link = invitationService.createInvitationLink(event.token());
        try {
            mailService.sendInvitation(event.email(), link);
            invitationService.markEmailSent(event.token());
            log.info("초대 메일 전송 완료. email={}, token={}", event.email(), event.token());
        }catch (Exception e) {
            log.error("초대 메일 전송 실패. email={}, token={}", event.email(), event.token(), e);
        }
    }
}
