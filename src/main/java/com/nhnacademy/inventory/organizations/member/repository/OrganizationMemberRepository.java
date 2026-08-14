package com.nhnacademy.inventory.organizations.member.repository;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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

    List<OrganizationMember> findAllByOrganizationId(Long organizationId);

    Optional<OrganizationMember> findByAccountUuidAndOrganizationId(UUID accountUuid, Long organizationId);

    Optional<OrganizationMember> findByIdAndOrganizationId(Long memberId, Long organizationId);

    @Query("""
        select om
        from OrganizationMember om
        where om.organization.id = :organizationId
        and not exists (
            select md
            from MemberDepartment md
            where md.organizationMember.id = om.id
        )
    """)
    List<OrganizationMember> findAllWithoutDepartment(Long organizationId);
}
