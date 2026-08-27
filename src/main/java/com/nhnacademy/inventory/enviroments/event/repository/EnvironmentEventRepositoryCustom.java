package com.nhnacademy.inventory.enviroments.event.repository;

import com.nhnacademy.inventory.enviroments.event.dto.EnvironmentEventInfoResponse;
import com.nhnacademy.inventory.enviroments.event.dto.EnvironmentEventSearchCondition;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnvironmentEventRepositoryCustom {
    Page<EnvironmentEventInfoResponse> findEnvironmentEvents(
            Zone targetZone,
            EnvironmentEventSearchCondition condition,
            Pageable pageable
    );
}
