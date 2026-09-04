package com.nhnacademy.inventory.organizations.notification.dto.request;

import com.nhnacademy.inventory.organizations.notification.domain.NotificationChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NotificationChannelPreferencesUpdateRequest(
        @NotEmpty(message = "알림 채널 설정은 하나 이상 필요합니다.")
        List<@Valid NotificationChannelPreferenceUpdateItem> channels
) {
    public record NotificationChannelPreferenceUpdateItem(
            @NotNull(message = "알림 채널은 필수입니다.")
            NotificationChannel channel,

            @NotNull(message = "알림 활성화 여부는 필수입니다.")
            Boolean enabled,

            String recipient
    ) {
    }
}
