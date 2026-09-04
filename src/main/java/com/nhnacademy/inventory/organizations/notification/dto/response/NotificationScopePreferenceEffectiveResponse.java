package com.nhnacademy.inventory.organizations.notification.dto.response;

import com.nhnacademy.inventory.organizations.notification.dto.NotificationScopeSource;

public record NotificationScopePreferenceEffectiveResponse(
        Long storageId,
        Long zoneId,
        boolean enabled,
        NotificationScopeSource source
) {
}
