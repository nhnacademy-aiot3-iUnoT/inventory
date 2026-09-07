package com.nhnacademy.inventory.organizations.member.repository;

import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationRole;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long>, OrganizationMemberCustom {
    Optional<OrganizationMember> findByAccountUuid(UUID accountUuid);

    List<OrganizationMember> findAllByOrganizationIdAndAccountUuidIn(Long organizationId, List<UUID> accountUuids);

    @Modifying
    @Query("""
        delete from OrganizationMember om
        where om.organization.id = :organizationId
    """)
    void deleteByOrganizationId(Long organizationId);

    Optional<OrganizationMember> findByIdAndOrganizationId(Long memberId, Long organizationId);

    @Query("""
        select om.accountUuid
        from OrganizationMember om
        where om.organization.id = :organizationId
    """)
    List<UUID> findAccountUuidsByOrganizationId(Long organizationId);

    @Query("""
        select om.accountUuid
        from OrganizationMember om
        where om.organization.id = :organizationId
    """)
    Optional<UUID> findAccountUuidByOrganizationId(Long organizationId);

    @Query("""
        select distinct om from OrganizationMember om
        join MemberDepartment md on md.organizationMember = om
        join StorageDepartment sd on sd.department = md.department
        where sd.storage.id = :storageId
    """)
    List<OrganizationMember> findMemberByStorageId(@Param("storageId") Long storageId);

    Collection<? extends OrganizationMember> findAllByOrganizationAndOrganizationRoleIn(Organization organization, Collection<OrganizationRole> organizationRoles);
}
