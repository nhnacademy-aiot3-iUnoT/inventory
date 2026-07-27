package com.nhnacademy.inventory.organizations.storage.repository;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StorageRepository extends JpaRepository<Storage, Long> {
    List<Storage> findAllByOrganizationAndStatusNot(Organization organization, StorageStatus status);

    Optional<Storage> findByIdAndOrganization(Long id, Organization organization);

    boolean existsByOrganizationAndNameAndStatusNot(Organization organization, String name, StorageStatus status);
}
