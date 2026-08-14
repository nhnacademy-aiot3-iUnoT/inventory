package com.nhnacademy.inventory.organizations.department.repository;

import com.nhnacademy.inventory.organizations.department.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Modifying
    @Query("""
        delete from Department d
        where d.organization.id = :organizationId
    """)
    void deleteByOrganizationId(Long organizationId);

    List<Department> findAllByOrganizationId(Long organizationId);
    Optional<Department> findByIdAndOrganizationId(Long departmentId, Long organizationId);

    boolean existsByOrganizationIdAndName(Long organizationId, String name);
    boolean existsByOrganizationIdAndNameAndIdNot(Long organizationId, String name, Long departmentId);
}
