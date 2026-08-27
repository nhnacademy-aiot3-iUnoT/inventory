package com.nhnacademy.inventory.organizations.storage.repository;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StoragePermissionRepository extends JpaRepository<Storage, Long> {

    @Query("SELECT COUNT(sd) > 0 " +
            "FROM StorageDepartment sd " +
            "JOIN MemberDepartment md ON sd.department = md.department " +
            "JOIN OrganizationMember om ON md.organizationMember = om " +
            "WHERE om.accountUuid = :accountUuid AND sd.storage.id = :storageId")
    boolean hasStoragePermission(@Param("accountUuid") UUID accountUuid,
                                 @Param("storageId") Long storageId);
}
