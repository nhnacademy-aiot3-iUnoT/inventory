package com.nhnacademy.inventory.organizations.invitation.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSearchRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.request.InvitationSignupRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.request.SignupCompensateRequest;
import com.nhnacademy.inventory.organizations.invitation.dto.response.*;
import com.nhnacademy.inventory.organizations.invitation.event.InvitationMailSendEvent;
import com.nhnacademy.inventory.organizations.invitation.exception.*;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
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
    public Invitation createInvitation(Organization organization, String email, boolean invitedByAdmin) {
        boolean duplicated = invitationRepository.existsActiveInvitation(organization.getId(), email, LocalDateTime.now());

        if (duplicated) {
            throw new InvitationAlreadyExistsException();
        }

        Invitation invitation = invitationRepository.save(Invitation.create(organization, email, invitedByAdmin));
        log.info("조직({}) : {} 초대 생성 완료. invitationId={}", organization.getBusinessNumber(), invitedByAdmin, invitation.getId());
        applicationEventPublisher.publishEvent(
                new InvitationMailSendEvent(
                        invitation.getEmail(),
                        invitation.getToken()
                )
        );

        return invitation;
    }

    /**
     * 토큰 검증 (ACTIVE + 만료 전)
     */
    public void validateInvitationToken(UUID token) {
        Invitation invitation = findInvitation(token);

        validateToken(invitation);
    }

    @Transactional
    public InvitationSignupResponse signupWithInvitation(InvitationSignupRequest request) {
        Invitation invitation = findInvitation(request.token());

        // 초대 토큰 검증
        validateToken(invitation);

        // 이메일 일치 여부 확인
        if (!invitation.getEmail().equals(request.email())) {
            throw new InvitationEmailMismatchException();
        }

        Organization organization = invitation.getOrganization();

        boolean isOwner = invitation.isInvitedByAdmin();
        // 조직원 생성
        if (isOwner) {
            orgMemberService.createOwner(organization, request.accountUuid());
        } else {
            orgMemberService.createMember(organization, request.accountUuid());
        }

        // 초대 토큰 사용
        invitation.use();

        return new InvitationSignupResponse(isOwner);
    }

    @Transactional
    public void compensateSignup(SignupCompensateRequest request) {
        Invitation invitation = findInvitation(request.token());

        orgMemberService.deleteOrganizationMember(request.accountUuid());

        invitation.restore();
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
     * 메일 발송 완료 처리
     */
    @Transactional
    public void markEmailSent(UUID token) {
        findInvitation(token).markEmailSent();
    }


    /**
     * 재전송 (ACTIVE + 만료 전)
     */
    @Transactional
    public void resendInvitation(Long invitationId) {
        Invitation invitation = findInvitationById(invitationId);

        validateOwnerAccess(invitation);
        resend(invitation);
    }

    @Transactional
    public void resendInvitationForAdmin(Long organizationId, Long invitationId) {
        Invitation invitation = findInvitationById(invitationId);

        validateAdminInvitationAccess(organizationId, invitation);

        resend(invitation);
    }

    /**
     * 취소 (ACTIVE + 만료 전)
     */
    @Transactional
    public void cancelInvitation(Long invitationId) {
        Invitation invitation = findInvitationById(invitationId);

        validateOwnerAccess(invitation);
        cancel(invitation);
    }

    @Transactional
    public void cancelInvitationForAdmin(Long organizationId, Long invitationId) {
        Invitation invitation = findInvitationById(invitationId);

        validateAdminInvitationAccess(organizationId, invitation);
        cancel(invitation);
    }


    /**
     * 재발급
     */
    @Transactional
    public void reissueInvitation(Long invitationId) {
        Invitation invitation = findInvitationById(invitationId);

        validateOwnerAccess(invitation);

        reissue(invitation);
    }

    @Transactional
    public void reissueInvitationForAdmin(Long organizationId, Long invitationId) {
        Invitation invitation = findInvitationById(invitationId);

        validateAdminInvitationAccess(organizationId, invitation);

        reissue(invitation);
    }

    private void reissue(Invitation oldInvitation) {
        oldInvitation.reissue();

        Invitation newInvitation = createInvitation(oldInvitation.getOrganization(), oldInvitation.getEmail(), oldInvitation.isInvitedByAdmin());

        log.info("회원({}) 초대 재발급", newInvitation.getEmail());
    }

    /**
     * 토큰으로 초대 내역 찾기
     */
    private Invitation findInvitation(UUID token) {
        return invitationRepository.findByToken(token)
                .orElseThrow(InvitationNotFoundException::new);
    }

    private Invitation findInvitationById(Long invitationId) {
        return invitationRepository.findById(invitationId)
                .orElseThrow(InvitationNotFoundException::new);
    }

    /**
     * 토큰이 유효한지
     */
    private void validateToken(Invitation invitation) {
        if (invitation.getInvitationStatus() != InvitationStatus.ACTIVE) {
            throw new InvalidInvitationException();
        }

        if (invitation.isExpired(LocalDateTime.now())) {
            throw new InvitationExpiredException();
        }
        log.info("초대({}) 토큰 유효", invitation.getId());
    }

    /**
     * owner가 자기 조직의 초대만 조작하도록 권한 검증
     */
    private void validateOwnerAccess(Invitation invitation) {
        OrganizationMember currentMember = orgMemberService.getCurrentOrganizationMember(UserContext.getUserUuid());

        if (currentMember.getOrganizationRole() != OrganizationRole.ORG_OWNER) {
            throw new ForbiddenException();
        }

        Long currentOrganizationId = currentMember.getOrganization().getId();
        Long invitationOrganizationId = invitation.getOrganization().getId();

        if (!currentOrganizationId.equals(invitationOrganizationId)) {
            throw new ForbiddenException();
        }
    }

    private void validateAdminInvitationAccess(Long organizationId, Invitation invitation) {
        if (!organizationId.equals(invitation.getOrganization().getId())) {
            throw new ForbiddenException();
        }

        // Admin이 초대한 사람이 아니면
        if (!invitation.isInvitedByAdmin()) {
            throw new ForbiddenException();
        }
    }

    private void resend(Invitation invitation) {
        validateToken(invitation);

        applicationEventPublisher.publishEvent(
                new InvitationMailSendEvent(invitation.getEmail(), invitation.getToken())
        );
    }

    private void cancel(Invitation invitation) {
        validateToken(invitation);
        invitation.cancel();
    }

}
