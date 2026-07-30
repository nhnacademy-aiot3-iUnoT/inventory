package com.nhnacademy.inventory.organizations.member.service;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
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

    public OrganizationMember getOrganizationMemberByUuid(UUID userId) {
        return orgMemberRepository.findByAccountUuid(userId)
                .orElseThrow(UserOrgNotFoundException::new);
    }
}
