package com.nhnacademy.inventory.organizations.member.repository;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrganizationMemberCustom {
    Page<OrganizationMember> findMembers(Long organizationId, List<UUID> accountUuids, OrganizationRole role, boolean hasDepartment, Pageable pageable);
}
