package com.nhnacademy.inventory.organizations.storage.repository;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findAllByOrganizationAndStatusNot(Organization organization, StorageStatus status);
    List<Storage> findAllByOrganizationAndNameContainingIgnoreCaseAndStatusNot(Organization organization, String name, StorageStatus status);

    Optional<Storage> findByIdAndOrganization(Long id, Organization organization);

    List<Storage> findAllByOrganizationId(Long organizationId);

    List<Storage> findAllByOrganization(Organization organization);

    boolean existsByOrganizationAndNameAndStatusNotAndIdNot(Organization organization, String name, StorageStatus status, Long id);

    boolean existsByOrganizationAndNameAndStatusNot(Organization organization, String name, StorageStatus status);

    @Modifying
    @Query("""
        update Storage s
        set s.status = 'CLOSED'
        where s.organization.id = :organizationId
    """)
    void closeByOrganizationId(Long organizationId);




    List<Storage> findAllByOrganizationAndStatus(Organization organization, StorageStatus status);

    @Query("SELECT s.id FROM Storage s WHERE s.organization.id = :organizationId AND s.status = 'ACTIVE'")
    List<Long> findActiveIdsByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT DISTINCT sd.storage.id FROM MemberDepartment md " +
            "JOIN md.department d " +
            "JOIN StorageDepartment sd ON sd.department = d " +
            "WHERE md.organizationMember.id = :organizationMemberId AND sd.storage.status = 'ACTIVE'")
    List<Long> findActiveIdsByOrganizationMemberId(@Param(value = "organizationMemberId") Long organizationMemberId);
}
