package com.nhnacademy.inventory.organizations.organization.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.invitation.domain.Invitation;
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
import com.nhnacademy.inventory.organizations.organization.exception.AlreadySetupOrganization;
import com.nhnacademy.inventory.organizations.organization.exception.OrgAlreadyExistsException;
import com.nhnacademy.inventory.organizations.organization.exception.OrgNotFoundException;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final InvitationService invitationService;
    private final OrganizationMemberService orgMemberService;
    private final OrganizationDeletionService orgDeletionService;

    /**
     * (Admin) 조직 생성
     * - OrgStatus = Pending
     */
    @Transactional
    public OrgCreateResponse createOrganization(OrgCreateRequest orgCreateRequest) {
        // 조직 중복 확인
        if(organizationRepository.existsByBusinessNumber(orgCreateRequest.businessNumber())) {
            throw new OrgAlreadyExistsException();
        }

        Organization createOrg = Organization.create(
                orgCreateRequest.businessNumber(),
                orgCreateRequest.name()
        );

        organizationRepository.save(createOrg);
        log.info("조직({}) : {} 생성 완료", createOrg.getBusinessNumber(), createOrg.getName());

        Invitation invitation = invitationService.createInvitation(createOrg, orgCreateRequest.email());
        log.info("조직({}) : owner 초대 생성 완료. invitationId={}", createOrg.getBusinessNumber(), invitation.getId());

        return OrgCreateResponse.from(createOrg);
    }

    /**
     * (Owner) 조직 초기화
     * 주소, 상세 설명 입력 -> OrgStatus = Active
     */
    @Transactional
    public void setupOrganization(OrganizationSetupRequest request) {
        Organization organization = getOrgAfterValidateOwner();

        if (organization.getStatus() != OrganizationStatus.PENDING) {
            throw new AlreadySetupOrganization();
        }

        organization.complete(request.zipCode(), request.roadAddress(), request.addressDetail(), request.description());
    }

    /**
     * 조직 조회
     * - 목록, 단건 (관리자, 사용자)
     */
    public Page<OrgSearchResponse> getOrganizationList(OrgSearchRequest request, Pageable pageable) {
        return organizationRepository.search(request, pageable);
    }

    public AdminOrgDetailResponse getOrganizationForAdmin(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(OrgNotFoundException::new);

        return AdminOrgDetailResponse.from(organization);
    }

    public OrgDetailResponse getOrganizationForUser() {
        return OrgDetailResponse.from(getCurrentOrganization());
    }

    /**
     * 조직 수정
     */
    @Transactional
    public void updateOrganizationStatus(OrgStatusUpdateRequest request) {
        Organization organization = getOrgAfterValidateOwner();

        organization.updateStatus(request.status());

        log.info("조직 상태 변경 {} -> {}", organization.getStatus(), request.status());
    }

    @Transactional
    public void updateOrganization(OrgUpdateRequest request) {
        Organization organization = getOrgAfterValidateOwner();

        organization.update(
                request.roadAddress(),
                request.zipCode(),
                request.addressDetail(),
                request.description()
        );

        log.info("조직({}) 정보 수정 완료", organization.getId());
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
            invitationService.deleteByOrganizationId(organizationId);
            organizationRepository.delete(organization);
            return;
        }
        orgDeletionService.softDelete(organization);
    }

    /**
     * Role : ORG_OWNER 검증 후 조직 반환
     */
    public Organization getOrgAfterValidateOwner() {
        OrganizationMember organizationMember = orgMemberService.getCurrentOrganizationMember(UserContext.getUserUuid());

        if(organizationMember.getOrganizationRole() != OrganizationRole.ORG_OWNER) {
            throw new ForbiddenException();
        }

        return organizationMember.getOrganization();
    }

    /**
     * 현재 로그인한 사용자가 소속된 조직
     */
    public Organization getCurrentOrganization() {
        OrganizationMember member = orgMemberService.getCurrentOrganizationMember(UserContext.getUserUuid());
        return member.getOrganization();
    }
}
