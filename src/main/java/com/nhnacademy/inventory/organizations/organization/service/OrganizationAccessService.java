package com.nhnacademy.inventory.organizations.organization.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.exception.UserOrgNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationAccessService {

    private final OrganizationMemberRepository orgMemberRepository;

    /**
     * 검증 후 조직원 반환
     */
    public OrganizationMember getCurrentMember() {
        return orgMemberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(UserOrgNotFoundException::new);
    }

    public OrganizationMember requireOwnerOrBoss() {
        OrganizationMember member = getCurrentMember();

        if (!(member.isOwner() || member.isBoss())) {
            throw new ForbiddenException();
        }

        return member;
    }

    public OrganizationMember requireOwnerOrBossOf(Long organizationId) {
        OrganizationMember member = requireOwnerOrBoss();

        if (!member.getOrganization().getId().equals(organizationId)) {
            throw new ForbiddenException();
        }

        return member;
    }

    public OrganizationMember requireBoss() {
        OrganizationMember member = getCurrentMember();

        if (!member.isBoss()) {
            throw new ForbiddenException();
        }

        return member;
    }

    public OrganizationMember requireOwner() {
        OrganizationMember member = getCurrentMember();

        if (!member.isOwner()) {
            throw new ForbiddenException();
        }

        return member;
    }

    /**
     * 검증 후 조직 반환
     */
    public Organization requireOwnerOrganization() {
        return requireOwner().getOrganization();
    }

    public Organization requireBossOrganization() {
        return requireBoss().getOrganization();
    }

    public Organization requireOwnerOrBossOrganization() {
        return requireOwnerOrBoss().getOrganization();
    }
}
