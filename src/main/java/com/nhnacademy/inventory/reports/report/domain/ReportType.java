package com.nhnacademy.inventory.reports.report.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
    WEEKLY("주간"),
    MONTHLY("월간");

    private final String name;
}
