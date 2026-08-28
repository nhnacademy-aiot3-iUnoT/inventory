package com.nhnacademy.inventory.organizations.zone.repository;

import com.nhnacademy.inventory.organizations.zone.domain.SensorType;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ThresholdRepository extends JpaRepository<ZoneThreshold, Long> {
    Optional<ZoneThreshold> findByZoneAndSensorType(Zone zone, SensorType sensorType);

    List<ZoneThreshold> findAllByZone(Zone zone);

    List<ZoneThreshold> findAllByZoneId(Long zoneId);

    @Query("SELECT zt FROM ZoneThreshold zt " +
            "JOIN FETCH zt.zone z " +
            "JOIN FETCH z.storage s " +
            "JOIN FETCH s.organization o " +
            "WHERE zt.zoneThresholdId = :zoneThresholdId AND zt.zone = :zone")
    Optional<ZoneThreshold> findByZoneThresholdIdAndZone(
            @Param("zoneThresholdId") Long zoneThresholdId,
            @Param("zone") Zone zone
    );
}
