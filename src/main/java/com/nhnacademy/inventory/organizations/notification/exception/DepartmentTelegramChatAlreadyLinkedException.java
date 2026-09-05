package com.nhnacademy.inventory.organizations.notification.exception;

import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.organizations.error.OrganizationErrorCode;

public class DepartmentTelegramChatAlreadyLinkedException extends BaseException {
    public DepartmentTelegramChatAlreadyLinkedException() {
        super(OrganizationErrorCode.DEPARTMENT_TELEGRAM_CHAT_ALREADY_LINKED);
    }
}
