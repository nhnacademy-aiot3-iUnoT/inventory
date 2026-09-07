package com.nhnacademy.inventory.assistant.rule;

import com.nhnacademy.inventory.assistant.domain.Severity;

// subject 와 detail 은 화면이 직접 그림. LLM 이 엮는 것은 explanation 뿐임
public record Finding(
        FindingType type,
        Severity severity,
        String subject,
        String detail,
        String explanation,
        TargetReference target
) {
}
