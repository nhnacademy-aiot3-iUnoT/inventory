package com.nhnacademy.inventory.reports.report.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrganizationMemberValidator {
     private final OrganizationMemberRepository memberRepository;

    // 임시로 만들어 둠. 추후 공용 서비스로 분리할지 논의 필요
    @Transactional(readOnly = true)
    public void validate(Long organizationId) {
        // memberRepository.findByOrganizationIdAndAccountUuid?
        OrganizationMember member = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);

        if (!Objects.equals(member.getOrganization().getId(), organizationId)) {
            throw new ForbiddenException();
        }
    }
}
