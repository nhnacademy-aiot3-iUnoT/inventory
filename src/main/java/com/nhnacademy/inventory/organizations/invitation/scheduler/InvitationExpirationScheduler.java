package com.nhnacademy.inventory.organizations.invitation.scheduler;

import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationExpirationScheduler {

    private final InvitationRepository invitationRepository;

    @Scheduled(fixedDelayString = "${app.invitation-expiration-interval-ms:60000}")
    @Transactional
    public void expireInvitations() {
        int updatedCount = invitationRepository.expireInvitations(InvitationStatus.ACTIVE, InvitationStatus.EXPIRED, LocalDateTime.now());

        if (updatedCount > 0) {
            log.info("초대 만료 상태 변경 완료. count={}", updatedCount);
        }
    }
}
