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

    @Modifying
    @Query("delete from MemberDepartment md " +
            "where md.department.id = :departmentId")
    void deleteByDepartmentId(Long departmentId);

    @Query("""
        select md
        from MemberDepartment md
        join fetch md.department
        where md.organizationMember.id = :memberId
    """)
    List<MemberDepartment> findAllWithDepartmentByMemberId(Long memberId);

    @Query("""
        select md
        from MemberDepartment md
        join fetch md.organizationMember
        where md.department.id = :departmentId
    """)
    List<MemberDepartment> findAllWithMemberByDepartmentId(Long departmentId);

    List<MemberDepartment> findAllByOrganizationMember(OrganizationMember organizationMember);

    void deleteByOrganizationMemberId(Long organizationMemberId);
    void deleteByDepartmentIdAndOrganizationMemberId(Long departmentId, Long organizationMemberId);

    boolean existsByDepartmentIdAndOrganizationMemberId(Long departmentId, Long organizationMemberId);

}
