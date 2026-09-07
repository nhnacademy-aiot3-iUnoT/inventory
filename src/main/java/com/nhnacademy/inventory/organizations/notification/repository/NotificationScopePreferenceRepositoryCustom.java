package com.nhnacademy.inventory.organizations.notification.repository;

import com.nhnacademy.inventory.organizations.notification.domain.NotificationScopePreference;

import java.util.List;
import java.util.Optional;

public interface NotificationScopePreferenceRepositoryCustom {


    List<NotificationScopePreference> findEnabledScopes(
            Long organizationId,
            Long storageId,
            Long zoneId
    );

    Optional<NotificationScopePreference> findScope(
            Long organizationMemberId,
            Long storageId,
            Long zoneId
    );

    Optional<NotificationScopePreference> findEffectiveScope(
            Long organizationMemberId,
            Long storageId,
            Long zoneId
    );

}
