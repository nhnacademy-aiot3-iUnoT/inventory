package com.nhnacademy.inventory.organizations.notification.repository;

import com.nhnacademy.inventory.organizations.notification.domain.NotificationScopePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationScopePreferenceRepository extends JpaRepository<NotificationScopePreference, Long>, NotificationScopePreferenceRepositoryCustom {

    List<NotificationScopePreference> findAllByOrganizationMemberId(Long organizationMemberId);

}
