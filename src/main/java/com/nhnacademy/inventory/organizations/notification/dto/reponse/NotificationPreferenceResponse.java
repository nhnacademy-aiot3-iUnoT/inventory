package com.nhnacademy.inventory.organizations.notification.dto.reponse;

import com.nhnacademy.inventory.organizations.notification.domain.NotificationChannel;

public record NotificationPreferenceResponse(
        Long userId,
        Long organizationId,
        Long storageId,
        Long zoneId,
        NotificationChannel channel,
        boolean enabled,
        String recipient
) {
}
