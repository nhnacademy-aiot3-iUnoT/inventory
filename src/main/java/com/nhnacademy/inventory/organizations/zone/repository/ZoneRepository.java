package com.nhnacademy.inventory.organizations.zone.repository;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findAllByStorageAndStatusNot(Storage storage, ZoneStatus status);

    Optional<Zone> findByIdAndStorage(Long id, Storage storage);

    List<Zone> findAllByStorage(Storage storage);

    List<Zone> findAllByStorageId(Long storageId);


    boolean existsByStorageAndNameAndStatusNotAndIdNot(Storage storage, String name, ZoneStatus status, Long id);

    boolean existsByStorageAndNameAndStatusNot(Storage storage, String name, ZoneStatus status);

    @Modifying
    @Query("""
        update Zone z
        set z.status = 'CLOSED'
        where z.storage.organization.id = :organizationId
    """)
    void closeByOrganizationId(Long organizationId);


    Long storage(Storage storage);
}
