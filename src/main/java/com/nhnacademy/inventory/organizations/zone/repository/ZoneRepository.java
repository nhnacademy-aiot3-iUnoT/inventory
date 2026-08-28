package com.nhnacademy.inventory.organizations.zone.repository;

import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    @Query("SELECT z FROM Zone z JOIN FETCH z.storage WHERE z.storage = :storage AND z.status != :status")
    List<Zone> findAllByStorageAndStatusNot(@Param("storage") Storage storage, @Param("status") ZoneStatus status);

    @Query("SELECT z FROM Zone z JOIN FETCH z.storage WHERE z.id = :id AND z.storage = :storage")
    Optional<Zone> findByIdAndStorage(@Param("id") Long id, @Param("storage") Storage storage);

    @Query("SELECT z FROM Zone z JOIN FETCH z.storage WHERE z.id = :id")
    Optional<Zone> findByIdWithStorage(@Param("id") Long id);

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
