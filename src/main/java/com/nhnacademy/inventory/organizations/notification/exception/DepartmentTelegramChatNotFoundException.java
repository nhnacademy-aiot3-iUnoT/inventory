package com.nhnacademy.inventory.organizations.notification.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class DepartmentTelegramChatNotFoundException extends BaseException {
    public DepartmentTelegramChatNotFoundException() {
        super(OrganizationErrorCode.DEPARTMENT_TELEGRAM_CHAT_NOT_FOUND);
    }
}
