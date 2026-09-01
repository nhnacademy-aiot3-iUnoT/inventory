package com.nhnacademy.inventory.assistant.exception;

import com.nhnacademy.inventory.assistant.error.AssistantErrorCode;
import com.nhnacademy.inventory.global.error.BaseException;

public class AssistantNoteNotFoundException extends BaseException {

    public AssistantNoteNotFoundException() {
        super(AssistantErrorCode.ASSISTANT_NOTE_NOT_FOUND);
    }
}
