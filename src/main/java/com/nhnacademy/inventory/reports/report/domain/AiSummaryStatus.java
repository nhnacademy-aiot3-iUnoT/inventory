package com.nhnacademy.inventory.reports.report.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiSummaryStatus {
    PENDING("생성 중"),
    COMPLETED("생성 완료"),
    FAILED("생성 실패");

    private final String description;
}
