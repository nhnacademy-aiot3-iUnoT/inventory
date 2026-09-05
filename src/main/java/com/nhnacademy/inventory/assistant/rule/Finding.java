package com.nhnacademy.inventory.assistant.rule;

import com.nhnacademy.inventory.assistant.domain.Severity;

/*
    subject 와 detail 은 화면이 직접 그린다. LLM 을 거치지 않아야 품목명과 로트번호가 사라지지 않는다.
    LLM 이 엮는 것은 explanation 뿐이다.
 */
public record Finding(
        FindingType type,
        Severity severity,
        String subject,
        String detail,
        String explanation,
        TargetReference target
) {
}
