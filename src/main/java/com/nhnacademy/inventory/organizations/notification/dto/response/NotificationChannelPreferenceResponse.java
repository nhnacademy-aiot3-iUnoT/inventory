package com.nhnacademy.inventory.organizations.notification.dto.response;

import com.nhnacademy.inventory.organizations.notification.domain.NotificationChannel;
import com.nhnacademy.inventory.organizations.notification.domain.NotificationChannelPreference;

public record NotificationChannelPreferenceResponse(
        NotificationChannel channel,
        String recipient,
        boolean enabled
) {
    public static NotificationChannelPreferenceResponse from(NotificationChannelPreference preference) {
        return new NotificationChannelPreferenceResponse(
                preference.getChannel(),
                preference.getRecipient(),
                preference.isEnabled()
        );
    }
}
