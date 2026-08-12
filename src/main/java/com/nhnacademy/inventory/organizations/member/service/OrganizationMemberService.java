package com.nhnacademy.inventory.organizations.member.service;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationMemberService {
    private final OrganizationMemberRepository orgMemberRepository;

    /**
     *  현재 로그인한 사용자의 조직원 정보
     */
    public OrganizationMember getCurrentOrganizationMember(UUID userId) {
        return orgMemberRepository.findByAccountUuid(userId)
                .orElseThrow(UserOrgNotFoundException::new);
    }

    /**
     * 조직원 생성
     */
    @Transactional
    public OrganizationMember createMember(Organization organization, UUID accountUuid) {
        OrganizationMember member = OrganizationMember.createMember(
                organization,
                accountUuid
        );
        return orgMemberRepository.save(member);
    }

    @Transactional
    public OrganizationMember createOwner(Organization organization, UUID accountUuid) {
        OrganizationMember owner = OrganizationMember.createOwner(
                organization,
                accountUuid
        );
        return orgMemberRepository.save(owner);
    }

    @Transactional
    public void deleteOrganizationMember(UUID accountUuid) {
        OrganizationMember member = orgMemberRepository.findByAccountUuid(accountUuid)
                .orElseThrow(UserOrgNotFoundException::new);

        orgMemberRepository.delete(member);
    }
}
