package com.nhnacademy.inventory.reports.report.controller;

import com.nhnacademy.inventory.global.dto.ApiResponse;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.reports.report.dto.ReportCreateRequest;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.usecase.ReportCreateFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
public class ReportController {
    private final ReportCreateFacade reportCreateFacade;

    @PostMapping("/reports/weekly")
    public ResponseEntity<ApiResponse<ReportInfoResponse>> createWeeklyReport(
            @Valid @RequestBody ReportCreateRequest request
    ) {
        UUID accountUuid = UserContext.getUserUuid();

        ReportInfoResponse response = reportCreateFacade.createWeeklyReport(accountUuid, request.periodStart());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
