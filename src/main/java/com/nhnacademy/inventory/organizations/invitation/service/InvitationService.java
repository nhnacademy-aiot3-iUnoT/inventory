package com.nhnacademy.inventory.organizations.invitation.service;

import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.invitation.exception.InvitationNotFoundException;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitationService {
    private final InvitationRepository invitationRepository;

    @Value("${app.invitation-url}")
    private String invitationUrl;

    /**
     * 초대 생성
     */
    @Transactional
    public Invitation createOwnerInvitation(Organization organization, String email) {
        // TODO(na) 중복 초대 검증 추가
        Invitation invitation = Invitation.create(organization, email);

        return invitationRepository.save(invitation);
    }

    public String createInvitationLink(UUID invitationToken) {
        return invitationUrl + "?token=" + invitationToken;
    }

    @Transactional
    public void markEmailSent(UUID invitationToken) {
        getInvitation(invitationToken).markEmailSent();
    }

    /**
     * 초대 조회
     */
    private Invitation getInvitation(UUID token) {
        return invitationRepository.findByToken(token)
                .orElseThrow(InvitationNotFoundException::new);
    }

    /**
     * 초대 토큰 검증
     */

}
