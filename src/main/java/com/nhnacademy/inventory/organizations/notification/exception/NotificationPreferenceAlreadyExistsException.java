package com.nhnacademy.inventory.organizations.notification.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class NotificationPreferenceAlreadyExistsException extends BaseException {
  public NotificationPreferenceAlreadyExistsException() {
    super(OrganizationErrorCode.NOTIFICATION_PREFERENCE_ALREADY_EXISTS);
  }
}
