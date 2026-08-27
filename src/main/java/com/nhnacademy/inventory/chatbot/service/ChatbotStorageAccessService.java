package com.nhnacademy.inventory.chatbot.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.department.repository.MemberDepartmentRepository;
import com.nhnacademy.inventory.organizations.member.exception.OrgMemberNotFoundException;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotStorageAccessService {
    private final OrganizationMemberRepository memberRepository;
    private final MemberDepartmentRepository memberDepartmentRepository;
    private final StorageRepository storageRepository;

    public List<Long> getAccessibleStorageIds() {
        OrganizationMember member = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(OrgMemberNotFoundException::new);

        if (member.getOrganizationRole() == OrganizationRole.ORG_BOSS) {
            return storageRepository.findIdsByOrganizationIdAndStatusNot(member.getOrganization().getId(), StorageStatus.CLOSED);
        }

        return memberDepartmentRepository.findAccessibleStorageIds(member.getId());
    }
}
