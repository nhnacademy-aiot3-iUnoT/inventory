package com.nhnacademy.inventory.organizations.member.service;

import com.nhnacademy.inventory.global.cient.AccountClient;
import com.nhnacademy.inventory.global.dto.account.AccountResponse;
import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.dto.request.OrganizationMemberSearchRequest;
import com.nhnacademy.inventory.organizations.member.dto.request.OrganizationRoleUpdateRequest;
import com.nhnacademy.inventory.organizations.member.dto.response.OrganizationMemberResponse;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.member.exception.OrgMemberNotFoundException;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationAccessService;
import com.nhnacademy.inventory.organizations.organization.service.OrganizationDeletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationMemberService {
    private final OrganizationMemberRepository orgMemberRepository;
    private final AccountClient accountClient;
    private final OrganizationDeletionService orgDeletionService;
    private final OrganizationAccessService orgAccessService;

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
        OrganizationMember member = orgMemberRepository.findByAccountUuid(accountUuid)
                .orElseThrow(UserOrgNotFoundException::new);

        orgMemberRepository.delete(member);
    }

    /**
     * 조회 (목록, 단건)
     */
    public Page<OrganizationMemberResponse> findMembers(OrganizationMemberSearchRequest request, boolean hasDepartment, Pageable pageable) {
        List<AccountResponse> accounts = null;
        List<UUID> accountUuids = null;

        OrganizationMember currentMember = orgAccessService.requireOwnerOrBoss();

        Long organizationId = currentMember.getOrganization().getId();

        // email 검색 O
        if(request.email() != null && !request.email().isBlank()) {
            accounts = accountClient.searchByEmail(request.email());

            if(accounts.isEmpty()) {
                return Page.empty(pageable);
            }

            accountUuids = accounts.stream()
                    .map(AccountResponse::accountUuid)
                    .toList();
        }

        Page<OrganizationMember> members = orgMemberRepository.findMembers(organizationId, accountUuids, hasDepartment ? request.role() : null, hasDepartment, pageable);

        if(accounts != null) {
            return toResponse(members, accounts);
        }

        return toResponse(members);
    }

    @Transactional
    public void updateRole(Long memberId, OrganizationRoleUpdateRequest roleUpdateRequest) {
        // Boss만 변경 가능
        OrganizationMember currentMember = orgAccessService.requireBoss();

        // Owner <-> Member
        OrganizationMember changeMember = getMemberById(memberId, currentMember.getOrganization().getId());
        changeMember.updateRole(roleUpdateRequest.role());
    }

    @Transactional
    public void deleteMember(Long memberId) {
        // Boss만 변경 가능
        OrganizationMember currentMember = orgAccessService.requireBoss();

        OrganizationMember deleteMember = getMemberById(memberId, currentMember.getOrganization().getId());
        orgDeletionService.deleteMember(deleteMember);
    }

    @Transactional
    public void leaveOrganization() {
        OrganizationMember currentMember = orgAccessService.getCurrentMember();

        orgDeletionService.deleteMember(currentMember);
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

    /**
     * 조직원 목록 조회 응답값 조립
     */
    private Page<OrganizationMemberResponse> toResponse(Page<OrganizationMember> members, List<AccountResponse> accounts) {
        Map<UUID, String> emailMap = accounts.stream()
                .collect(Collectors.toMap(AccountResponse::accountUuid, AccountResponse::email));

        return members.map(member ->
                new OrganizationMemberResponse(
                        member.getId(),
                        emailMap.get(member.getAccountUuid()),
                        member.getOrganizationRole(),
                        member.getJoinedAt()
                )
        );
    }

    private Page<OrganizationMemberResponse> toResponse(Page<OrganizationMember> members) {
        if (members.isEmpty()) {
            return Page.empty(members.getPageable());
        }

        List<UUID> accountUuids = members.getContent()
                .stream()
                .map(OrganizationMember::getAccountUuid)
                .toList();

        List<AccountResponse> accounts = accountClient.findByUuids(accountUuids);

        return toResponse(members, accounts);
    }
}
