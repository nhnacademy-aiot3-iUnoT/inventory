package com.nhnacademy.inventory.organizations.zone.repository;

import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
}
