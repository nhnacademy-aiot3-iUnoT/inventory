package com.nhnacademy.inventory.organizations.member.repository;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
    Optional<OrganizationMember> findByAccountUuid(UUID accountUuid);

    @Modifying
    @Query("""
        delete from OrganizationMember om
        where om.organization.id = :organizationId
    """)
    void deleteByOrganizationId(Long organizationId);
}
