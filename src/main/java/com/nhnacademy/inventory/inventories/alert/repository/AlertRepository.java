package com.nhnacademy.inventory.inventories.alert.repository;

import com.nhnacademy.inventory.inventories.alert.domain.Alert;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long>, AlertRepositoryCustom{
    long countByOrganizationAndIsRead(Organization organization, Boolean isRead);

    List<Alert> findAllByIdInAndOrganization(Collection<Long> ids, Organization organization);

    List<Alert> findAllByOrganization(Organization organization);
}
