package com.nhnacademy.inventory.enviroments.event.repository;

import com.nhnacademy.inventory.enviroments.event.domain.EnvironmentEvent;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EnvironmentEventRepository extends JpaRepository<EnvironmentEvent, Long>, EnvironmentEventRepositoryCustom {
    List<EnvironmentEvent> findAllByZoneAndCreatedAtAfterOrderByCreatedAtDesc(Zone zone, LocalDateTime createdAtAfter);

    List<EnvironmentEvent> findAllByZoneAndCreatedAtAfter(Zone zone, LocalDateTime createdAtAfter);

    List<EnvironmentEvent> findAllByZoneAndCreatedAtBetween(Zone zone, LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);
}
