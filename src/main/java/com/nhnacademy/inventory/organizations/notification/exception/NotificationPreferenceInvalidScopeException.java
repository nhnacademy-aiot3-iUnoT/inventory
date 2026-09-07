package com.nhnacademy.inventory.organizations.notification.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class NotificationPreferenceInvalidScopeException extends BaseException {
    public NotificationPreferenceInvalidScopeException() {
        super(OrganizationErrorCode.NOTIFICATION_PREFERENCE_INVALID_SCOPE);
    }
}
