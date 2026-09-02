package com.nhnacademy.inventory.inventories.alert.repository;

import com.nhnacademy.inventory.inventories.alert.domain.Alert;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long>, AlertRepositoryCustom{
    long countByOrganizationAndIsChecked(Organization organization, Boolean isChecked);

    long countByOrganizationAndAlertTypeAndCreatedAtBetween(
            Organization organization, AlertType alertType, LocalDateTime start, LocalDateTime end);

    long countByOrganizationAndAlertTypeAndIsCheckedAndCreatedAtBetween(
            Organization organization, AlertType alertType, Boolean isChecked,
            LocalDateTime start, LocalDateTime end);


    List<Alert> findAllByIdInAndOrganization(Collection<Long> ids, Organization organization);

    List<Alert> findAllByOrganization(Organization organization);
}
