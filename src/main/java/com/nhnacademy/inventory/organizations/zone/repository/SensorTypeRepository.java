package com.nhnacademy.inventory.organizations.zone.repository;

import com.nhnacademy.inventory.organizations.zone.domain.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorTypeRepository extends JpaRepository<SensorType, Long> {
    Optional<SensorType> findByName(String name);
}
