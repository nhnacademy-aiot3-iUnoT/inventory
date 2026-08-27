package com.nhnacademy.inventory.organizations.department.repository;

import com.nhnacademy.inventory.organizations.department.domain.StorageDepartment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

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

    @Query("""
        select sd
        from StorageDepartment sd
        join fetch sd.storage
        where sd.department.id = :departmentId
    """)
    List<StorageDepartment> findAllWithStorageByDepartmentId(Long departmentId);

    @Query("""
    select sd
    from StorageDepartment sd
    join fetch sd.department
    where sd.storage.id = :storageId
""")
    List<StorageDepartment> findAllWithDepartmentByStorageId(Long storageId);


    @EntityGraph(attributePaths = "storage")
    List<StorageDepartment> findAllByDepartmentIdIn(List<Long> departmentIds);




    @Modifying
    @Query("""
        delete from StorageDepartment sd
        where sd.department.id = :departmentId
          and sd.storage.id = :storageId
    """)
    void deleteByDepartmentIdAndStorageId(Long departmentId, Long storageId);

    boolean existsByDepartmentIdAndStorageId(Long departmentId, Long storageId);
    Optional<StorageDepartment> findByDepartmentId(Long departmentId);
}
