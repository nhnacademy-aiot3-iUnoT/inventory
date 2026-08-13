package com.nhnacademy.inventory.organizations.sensor.repository;

import com.nhnacademy.inventory.organizations.sensor.domain.ZoneSensor;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ZoneSensorRepository extends JpaRepository<ZoneSensor, Long> {
    boolean existsByDeviceEui(String deviceEui);

    List<ZoneSensor> findAllByZone(Zone zone);

    Optional<ZoneSensor> findByIdAndZone(Long id, Zone zone);

    @Query("SELECT zs FROM ZoneSensor zs " +
            "JOIN FETCH zs.zone z " +
            "JOIN FETCH z.storage s " +
            "JOIN FETCH s.organization " +
            "WHERE zs.deviceEui = :deviceEui")
    Optional<ZoneSensor> findByDeviceEuiWithLocation(@Param("deviceEui") String deviceEui);
}
