package com.nhnacademy.inventory.organizations.notification.dto.request;

public record NotificationScopePreferenceRequest(
        Long storageId,
        Long zoneId,
        Boolean enabled
) {
}
