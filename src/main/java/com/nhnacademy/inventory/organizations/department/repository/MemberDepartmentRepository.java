package com.nhnacademy.inventory.organizations.department.repository;

import com.nhnacademy.inventory.organizations.department.domain.MemberDepartment;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberDepartmentRepository extends JpaRepository<MemberDepartment, Long> {

    @Modifying
    @Query("""
        delete from MemberDepartment md
        where md.organizationMember.organization.id = :organizationId
    """)
    void deleteByOrganizationId(Long organizationId);

    List<MemberDepartment> findAllByOrganizationMember(OrganizationMember organizationMember);



    void deleteByOrganizationMemberId(Long organizationMemberId);

    List<MemberDepartment> findAllByOrganizationMemberId(Long organizationMemberId);

}
