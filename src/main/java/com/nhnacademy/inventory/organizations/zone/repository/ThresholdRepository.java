package com.nhnacademy.inventory.organizations.zone.repository;

import com.nhnacademy.inventory.organizations.zone.domain.SensorType;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ThresholdRepository extends JpaRepository<ZoneThreshold, Long> {
    Optional<ZoneThreshold> findByZoneAndSensorType(Zone zone, SensorType sensorType);

    List<ZoneThreshold> findAllByZone(Zone zone);

    List<ZoneThreshold> findAllByZoneId(Long zoneId);

    Optional<ZoneThreshold> findByZoneThresholdIdAndZone(Long zoneThresholdId, Zone zone);
}
