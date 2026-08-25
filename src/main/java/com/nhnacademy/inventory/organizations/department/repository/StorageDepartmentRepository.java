package com.nhnacademy.inventory.organizations.department.repository;

import com.nhnacademy.inventory.organizations.department.domain.StorageDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

import java.util.Optional;

public interface StorageDepartmentRepository extends JpaRepository<StorageDepartment, Long> {
    @Modifying
    @Query("""
        delete from StorageDepartment sd
        where sd.storage.organization.id = :organizationId
    """)
    void deleteByOrganizationId(Long organizationId);

    @Modifying
    @Query("delete from StorageDepartment sd " +
            "where sd.department.id = :departmentId")
    void deleteByDepartmentId(Long departmentId);

    List<StorageDepartment> findAllByDepartmentId(Long departmentId);
    List<StorageDepartment> findAllByStorageId(Long storageId);
    boolean existsByDepartmentIdAndStorageId(Long departmentId, Long storageId);

    @Modifying
    @Query("""
        delete from StorageDepartment sd
        where sd.department.id = :departmentId
          and sd.storage.id = :storageId
    """)
    void deleteByDepartmentIdAndStorageId(Long departmentId, Long storageId);


    Optional<StorageDepartment> findByDepartmentId(Long departmentId);
}
