package com.nhnacademy.inventory.reports.report.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record ReportCreateRequest(
        @PastOrPresent(message = "리포트 시작일은 미래 날짜일 수 없습니다.")
        @NotNull(message = "리포트 시작일은 필수입니다.")
        LocalDate periodStart
) {}
