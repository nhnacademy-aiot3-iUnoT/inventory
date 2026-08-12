package com.nhnacademy.inventory.reports.report.dto;

import java.time.LocalDate;

public record ReportCreateRequest(
        LocalDate periodStart
) {}
