package com.nhnacademy.inventory.organizations.notification.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class NotificationRecipientRequiredException extends BaseException {
    public NotificationRecipientRequiredException() {
        super(OrganizationErrorCode.NOTIFICATION_RECIPIENT_REQUIRED);
    }
}
