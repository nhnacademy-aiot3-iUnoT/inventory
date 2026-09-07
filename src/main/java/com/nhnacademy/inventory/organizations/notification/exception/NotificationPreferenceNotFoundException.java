package com.nhnacademy.inventory.organizations.notification.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class NotificationPreferenceNotFoundException extends BaseException {
    public NotificationPreferenceNotFoundException() {
        super(OrganizationErrorCode.NOTIFICATION_PREFERENCE_NOT_FOUND);
    }
}
