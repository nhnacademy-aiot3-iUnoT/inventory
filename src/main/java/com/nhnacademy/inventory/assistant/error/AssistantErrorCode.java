package com.nhnacademy.inventory.assistant.error;

import com.nhnacademy.inventory.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AssistantErrorCode implements ErrorCode {
    ASSISTANT_NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "AS001", "존재하지 않는 알림입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
