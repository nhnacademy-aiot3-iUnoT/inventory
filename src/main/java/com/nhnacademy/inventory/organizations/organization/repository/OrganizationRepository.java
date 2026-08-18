package com.nhnacademy.inventory.organizations.organization.repository;

import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long>, OrganizationRepositoryCustom {
    boolean existsByBusinessNumber(String businessNumber);
}
