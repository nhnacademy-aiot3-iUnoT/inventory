package com.nhnacademy.inventory.organizations.invitation.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSearchRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.response.*;
import com.nhnacademy.inventory.organizations.invitation.event.InvitationMailSendEvent;
import com.nhnacademy.inventory.organizations.invitation.exception.InvalidInvitationException;
import com.nhnacademy.inventory.organizations.invitation.exception.InvitationEmailMismatchException;
import com.nhnacademy.inventory.organizations.invitation.exception.InvitationExpiredException;
import com.nhnacademy.inventory.organizations.invitation.exception.InvitationNotFoundException;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrganizationMemberService orgMemberService;

    @Value("${app.invitation-url}")
    private String invitationUrl;

    /**
     * 초대 생성 및 메일 전송 이벤트 발행
     */
    @Transactional
    public Invitation createInvitation(Organization organization, String email) {
        Invitation invitation = invitationRepository.save(Invitation.create(organization, email));
        log.info("조직({}) : owner 초대 생성 완료. invitationId={}", organization.getBusinessNumber(), invitation.getId());

        applicationEventPublisher.publishEvent(
                new InvitationMailSendEvent(
                        invitation.getEmail(),
                        invitation.getToken()
                )
        );

        return invitation;
    }

    /**
     * 토큰 검증
     */
    public InvitationVerifyResponse validateInvitationToken(UUID token) {
        Invitation invitation = findInvitation(token);

        validateToken(invitation);

        return new InvitationVerifyResponse(
                invitation.getEmail(),
                invitation.getOrganization().getName()
        );
    }

    public void validateInvitationForSignup(UUID token, String email) {
        Invitation invitation = findInvitation(token);

        validateToken(invitation);

        if(!invitation.getEmail().equals(email)) {
            throw new InvitationEmailMismatchException();
        }
    }

    /**
     * 초대 목록 조회
     */
    public Page<InvitationSearchResponse> getInvitations(InvitationSearchRequest request, Pageable pageable) {
        OrganizationMember member = orgMemberService.getCurrentOrganizationMember(UserContext.getUserUuid());

        if(member.getOrganizationRole() != OrganizationRole.ORG_OWNER) {
            throw new ForbiddenException();
        }

        return invitationRepository.search(member.getOrganization().getId(), request, pageable);
    }

    /**
     * 초대 링크 생성
     */
    public String createInvitationUrl(UUID token) {
        return invitationUrl + "?token=" + token;
    }


    /**
     * 회원가입 완료 후 사용 처리
     */
    @Transactional
    public void useInvitation(UUID token) {
        Invitation invitation = findInvitation(token);
        invitation.use();
    }


    /**
     * 메일 발송 완료 처리
     */
    @Transactional
    public void markEmailSent(UUID token) {
        findInvitation(token).markEmailSent();
    }


    /**
     * 재전송
     */
    @Transactional
    public void resendInvitation(UUID token) {
        Invitation invitation = findInvitation(token);

        validateActive(invitation);

        // TODO(na) event 따로 처리할지 고민 중
        applicationEventPublisher.publishEvent(
            new InvitationMailSendEvent(invitation.getEmail(), invitation.getToken())
        );
    }


    /**
     * 취소
     */
    @Transactional
    public void cancelInvitation(UUID token) {
        Invitation invitation = findInvitation(token);

        validateActive(invitation);

        invitation.cancel();
    }


    /**
     * 재발급
     */
    @Transactional
    public InvitationCreateResponse reissueInvitation(UUID token) {
        Invitation oldInvitation = findInvitation(token);
        validateActive(oldInvitation);
        oldInvitation.cancel();

        Invitation newInvitation = createInvitation(oldInvitation.getOrganization(), oldInvitation.getEmail());

        return InvitationCreateResponse.from(newInvitation);
    }

    @Transactional
    public void deleteByOrganizationId(Long organizationId) {
        invitationRepository.deleteByOrganizationId(organizationId);
    }

    /**
     * 토큰으로 초대 내역 찾기
     */
    private Invitation findInvitation(UUID token) {
        return invitationRepository.findByToken(token)
                .orElseThrow(InvitationNotFoundException::new);
    }

    /**
     * 토큰이 유효한지
     */
    private void validateToken(Invitation invitation) {
       // 초대 상태 active인지
        if(invitation.getInvitationStatus() != InvitationStatus.ACTIVE) {
            throw new InvalidInvitationException();
        }
        // 만료 전
        if(invitation.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new InvitationExpiredException();
        }
    }


    private void validateActive(Invitation invitation) {
        if(invitation.getInvitationStatus() != InvitationStatus.ACTIVE) {
            throw new InvalidInvitationException();
        }
    }
}
