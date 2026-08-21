package com.nhnacademy.inventory.organizations.storage.repository;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findAllByOrganizationAndStatusNot(Organization organization, StorageStatus status);

    Optional<Storage> findByIdAndOrganization(Long id, Organization organization);

    List<Storage> findAllByOrganizationId(Long organizationId);

    boolean existsByOrganizationAndNameAndStatusNotAndIdNot(Organization organization, String name, StorageStatus status, Long id);

    boolean existsByOrganizationAndNameAndStatusNot(Organization organization, String name, StorageStatus status);

    @Modifying
    @Query("""
        update Storage s
        set s.status = 'CLOSED'
        where s.organization.id = :organizationId
    """)
    void closeByOrganizationId(Long organizationId);




}
