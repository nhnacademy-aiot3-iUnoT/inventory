package com.nhnacademy.inventory.organizations.organization.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgCreateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgSearchRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.request.OrgUpdateRequest;
import com.nhnacademy.inventory.organizations.organization.dto.response.AdminOrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgCreateResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgSearchResponse;
import com.nhnacademy.inventory.organizations.organization.dto.response.OrgDetailResponse;
import com.nhnacademy.inventory.organizations.organization.exception.OrgAlreadyExistsException;
import com.nhnacademy.inventory.organizations.organization.exception.OrgNotFoundException;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository orgMemberRepository;

    /**
     * 조직 생성
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

        // TODO(na) 메일 전송 추가

        return OrgCreateResponse.from(createOrg);
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

    // 유저 아이디 -> 조직 ID -> 조직 정보 출력
    public OrgDetailResponse getOrganizationForUser(UUID userId) {
        OrganizationMember organizationMember = orgMemberRepository.findByAccountUuid(userId)
                .orElseThrow(UserOrgNotFoundException::new);

        Organization organization = organizationMember.getOrganization();
        log.info("사용자 {}의 조직 ID = {}", userId, organization.getId());

        return OrgDetailResponse.from(organization);
    }

    /**
     * 조직 수정
     */
    @Transactional
    public void updateOrganizationStatus(UUID userId, OrgStatusUpdateRequest request) {
        OrganizationMember organizationMember = orgMemberRepository.findByAccountUuid(userId)
                .orElseThrow(UserOrgNotFoundException::new);

        if(!organizationMember.getOrganizationRole().equals(OrganizationRole.ORG_OWNER)) {
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
        OrganizationMember organizationMember = orgMemberRepository.findByAccountUuid(userId)
                .orElseThrow(UserOrgNotFoundException::new);

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
        if(organization.getStatus().equals(OrganizationStatus.PENDING)) {
            organizationRepository.delete(organization);
        }

        // 생성 후 : soft delete
        organization.suspend();
    }
}
