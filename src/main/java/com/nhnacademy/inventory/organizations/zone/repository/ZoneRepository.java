package com.nhnacademy.inventory.organizations.zone.repository;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findAllByStorageAndStatusNot(Storage storage, ZoneStatus status);

    Optional<Zone> findByIdAndStorage(Long id, Storage storage);

    boolean existsByStorageAndNameAndStatusNotAndIdNot(Storage storage, String name, ZoneStatus status, Long id);

    boolean existsByStorageAndNameAndStatusNot(Storage storage, String name, ZoneStatus status);
}
