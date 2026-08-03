package com.nhnacademy.inventory.organizations.organization.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
import com.nhnacademy.inventory.organizations.invitation.domain.InvitationStatus;
import com.nhnacademy.inventory.organizations.invitation.event.OwnerInvitationCreatedEvent;
import com.nhnacademy.inventory.organizations.invitation.repository.InvitationRepository;
import com.nhnacademy.inventory.organizations.invitation.service.InvitationService;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.service.OrganizationMemberService;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.organization.dto.request.*;
import com.nhnacademy.inventory.organizations.organization.dto.response.AdminOrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgCreateResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgSearchResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.exception.OrgAlreadyCompletedException;
import com.nhnacademy.inventory.organizations.organization.exception.OrgAlreadyExistsException;
import com.nhnacademy.inventory.organizations.organization.exception.OrgNotFoundException;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final InvitationRepository invitationRepository;

    private final InvitationService invitationService;
    private final OrganizationMemberService orgMemberService;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * (Admin) 조직 생성
     * - OrgStatus = Pending
     */
    @Transactional
    public OrgCreateResponse createOrganization(OrgCreateRequest orgCreateRequest) {
        // 조직 중복 확인
        if(organizationRepository.existsByBusinessNumber(orgCreateRequest.businessNumber())) {
            log.debug("사업자 번호({}) 중복. 조직 생성 실패", orgCreateRequest.businessNumber());
            throw new OrgAlreadyExistsException();
        }

        Organization createOrg = Organization.create(
                orgCreateRequest.businessNumber(),
                orgCreateRequest.name()
        );

        organizationRepository.save(createOrg);

        log.info("조직({}) : {} 생성 완료", createOrg.getBusinessNumber(), createOrg.getName());

        Invitation ownerInvitation = invitationService.createOwnerInvitation(createOrg, orgCreateRequest.email());

        // 초대 생성 알림
        applicationEventPublisher.publishEvent(
                new OwnerInvitationCreatedEvent(ownerInvitation.getEmail(), ownerInvitation.getToken())
        );

        return OrgCreateResponse.from(createOrg);
    }

    /**
     * (Owner) 조직 생성
     * 주소, 상세 설명 입력 -> OrgStatus = Active
     */
    @Transactional
    public void completeOrganization(UUID memberId, OrganizationCompleteRequest request) {

        OrganizationMember organizationMember = orgMemberService.getOrganizationMemberByUuid(memberId);

        if (!organizationMember.getOrganizationRole().equals(OrganizationRole.ORG_OWNER)) {
            throw new ForbiddenException();
        }

        Organization organization = organizationMember.getOrganization();

        if (!organization.getStatus().equals(OrganizationStatus.PENDING)) {
            throw new OrgAlreadyCompletedException();
        }

        organization.complete(request.zipCode(), request.roadAddress(), request.addressDetail());
    }

    /**
     * 조직 조회
     */
    public Page<OrgSearchResponse> getOrganizationList(OrgSearchRequest request, Pageable pageable) {
        return organizationRepository.search(request, pageable);
    }

    public AdminOrgDetailResponse getOrganizationForAdmin(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(OrgNotFoundException::new);

        return AdminOrgDetailResponse.from(organization);
    }

    public OrgDetailResponse getOrganizationForUser(UUID userId) {
        OrganizationMember organizationMember = orgMemberService.getOrganizationMemberByUuid(userId);
        Organization organization = organizationMember.getOrganization();
        log.info("사용자 {}의 조직 ID = {}", userId, organization.getId());

        return OrgDetailResponse.from(organization);
    }

    /**
     * 조직 수정
     */
    @Transactional
    public void updateOrganizationStatus(UUID userId, OrgStatusUpdateRequest request) {
        OrganizationMember organizationMember = orgMemberService.getOrganizationMemberByUuid(userId);

        if(organizationMember.getOrganizationRole() != OrganizationRole.ORG_OWNER) {
            log.debug("조직 수정 권한 없음");
            throw new ForbiddenException();
        }

        Organization organization = organizationMember.getOrganization();
        OrganizationStatus originStatus = organization.getStatus();

        organization.updateStatus(request.status());
        log.info("조직 상태 변경 {} -> {}", originStatus, request.status());
    }

    @Transactional
    public void updateOrganization(UUID userId, OrgUpdateRequest request) {
        OrganizationMember organizationMember = orgMemberService.getOrganizationMemberByUuid(userId);

        if(!organizationMember.getOrganizationRole().equals(OrganizationRole.ORG_OWNER)) {
            log.debug("조직 수정 권한 없음");
            throw new ForbiddenException();
        }

        Organization organization = organizationMember.getOrganization();

        organization.update(
                request.roadAddress(),
                request.zipCode(),
                request.addressDetail(),
                request.description()
        );
    }

    /**
     * 조직 삭제
     */
    @Transactional
    public void deleteOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(OrgNotFoundException::new);

        // Owner 조직 생성 전 : hard delete
        if(organization.getStatus() == OrganizationStatus.PENDING) {
            invitationRepository.deleteByOrganizationId(organizationId);
            organizationRepository.delete(organization);
            return;
        }

        // 생성 후 : soft delete
        organization.suspend();
        List<Invitation> invitations = invitationRepository.findByOrganizationIdAndInvitationStatus(organizationId, InvitationStatus.ACTIVE);
        invitations.forEach(Invitation::cancel);
    }
}
