package com.nhnacademy.inventory.organizations.member.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.dto.response.OrganizationMemberListResponse;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.exception.OrgMemberNotFoundException;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationDeletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationMemberService {
    private final OrganizationMemberRepository orgMemberRepository;
    private final OrganizationDeletionService orgDeletionService;

    /**
     * 조직원 생성
     */
    @Transactional
    public void createUser(Organization organization, UUID accountUuid, OrganizationRole role) {
        OrganizationMember user = OrganizationMember.createUser(
                organization,
                accountUuid,
                role
        );
        orgMemberRepository.save(user);
    }


    @Transactional
    public void compensateMember(UUID accountUuid) {
        Organization organization = getCurrentOrganizationMember(accountUuid).getOrganization();

        OrganizationMember member = orgMemberRepository
                .findByAccountUuidAndOrganizationId(accountUuid, organization.getId())
                .orElseThrow(UserOrgNotFoundException::new);

        orgMemberRepository.delete(member);
    }

    /**
     * OWNER 권한 확인
     */
    private void checkOwner(OrganizationMember member) {
        if (!member.isOwner()) {
            throw new ForbiddenException();
        }
    }

    /**
     * 조회 (목록, 단건)
     */
    public List<OrganizationMemberListResponse> getMembers() {
        OrganizationMember currentMember = getCurrentOrganizationMember(UserContext.getUserUuid());

        checkOwner(currentMember);

        Organization organization = currentMember.getOrganization();

        return orgMemberRepository.findAllByOrganizationId(organization.getId())
                .stream()
                .map(OrganizationMemberListResponse::from)
                .toList();
    }

    public OrganizationMember getMemberById(Long memberId, Long organizationId) {
        return orgMemberRepository.findByIdAndOrganizationId(memberId, organizationId)
                .orElseThrow(OrgMemberNotFoundException::new);
    }

    /**
     *  현재 로그인한 사용자의 조직원 정보
     */
    public OrganizationMember getCurrentOrganizationMember(UUID userId) {
        return orgMemberRepository.findByAccountUuid(userId)
                .orElseThrow(UserOrgNotFoundException::new);
    }


    public List<OrganizationMemberListResponse> getMembersWithoutDepartment() {
        OrganizationMember currentMember = getCurrentOrganizationMember(UserContext.getUserUuid());

        checkOwner(currentMember);

        Long organizationId = currentMember.getOrganization().getId();

        return orgMemberRepository.findAllWithoutDepartment(organizationId)
                .stream()
                .map(OrganizationMemberListResponse::from)
                .toList();
    }
}
