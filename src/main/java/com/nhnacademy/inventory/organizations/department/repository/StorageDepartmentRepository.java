package com.nhnacademy.inventory.organizations.department.repository;

import com.nhnacademy.inventory.organizations.department.domain.StorageDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StorageDepartmentRepository extends JpaRepository<StorageDepartment, Long> {
    @Modifying
    @Query("""
        delete from StorageDepartment sd
        where sd.storage.organization.id = :organizationId
    """)
    void deleteByOrganizationId(Long organizationId);
}
