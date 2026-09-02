package com.nhnacademy.inventory.inventories.alert.repository;

import com.nhnacademy.inventory.inventories.alert.domain.Alert;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long>, AlertRepositoryCustom{
    long countByOrganizationMemberAndIsChecked(OrganizationMember organizationMember, Boolean isChecked);

    List<Alert> findAllByIdInAndOrganizationMember(Collection<Long> ids, OrganizationMember organizationMember);

    List<Alert> findAllByOrganizationMember(OrganizationMember organizationMember);

}
