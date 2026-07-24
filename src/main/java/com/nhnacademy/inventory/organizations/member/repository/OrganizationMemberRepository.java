package com.nhnacademy.inventory.organizations.member.repository;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
}
