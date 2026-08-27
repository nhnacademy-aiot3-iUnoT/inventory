package com.nhnacademy.inventory.reports.report.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiSummaryStatus {
    PENDING("대기 중"),
    PROGRESS("생성 중"),
    COMPLETED("생성 완료"),
    FAILED("생성 실패");

    private final String description;
}
