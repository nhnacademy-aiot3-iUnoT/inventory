package com.nhnacademy.inventory.organizations.notification.dto.response;

import com.nhnacademy.inventory.organizations.notification.domain.NotificationScopePreference;

/**
 * 조직원이 직접 설정한 알림 범위 한 건.
 * 엔티티를 그대로 내보내면 지연 로딩된 연관관계가 딸려 나오므로 필요한 값만 담는다.
 */
public record NotificationScopePreferenceResponse(
        Long storageId,
        Long zoneId,
        boolean enabled
) {
    public static NotificationScopePreferenceResponse from(NotificationScopePreference preference) {
        return new NotificationScopePreferenceResponse(
                preference.getStorage() == null ? null : preference.getStorage().getId(),
                preference.getZone() == null ? null : preference.getZone().getId(),
                preference.isEnabled()
        );
    }
}
