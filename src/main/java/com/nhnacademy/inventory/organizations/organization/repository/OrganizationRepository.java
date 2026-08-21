package com.nhnacademy.inventory.organizations.organization.repository;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long>, OrganizationRepositoryCustom {
    boolean existsByBusinessNumber(String businessNumber);

    List<Organization> findAllByStatus(OrganizationStatus status);
}
