package com.nhnacademy.inventory.reports.report.controller;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.reports.report.domain.AiSummaryStatus;
import com.nhnacademy.inventory.reports.report.domain.ReportType;
import com.nhnacademy.inventory.reports.report.dto.ReportCreateRequest;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.usecase.ReportCreateFacade;
import com.nhnacademy.inventory.reports.report.usecase.ReportGetUseCase;
import com.nhnacademy.inventory.reports.report.usecase.ReportRetrySummaryUseCase;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest extends SupportControllerTest {

    @MockitoBean
    private ReportCreateFacade reportCreateFacade;

    @MockitoBean
    private ReportGetUseCase reportGetUseCase;

    @MockitoBean
    private ReportRetrySummaryUseCase reportRetrySummaryUseCase;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("주간 리포트를 생성하면 리포트 정보를 반환한다.")
    void createWeeklyReport() throws Exception {
        // given
        UUID accountUuid = UUID.randomUUID();
        UserContext.setUserUuid(accountUuid);

        long organizationId = 1L;
        LocalDate periodStart = LocalDate.of(2026, Month.AUGUST, 10);
        ReportCreateRequest request = new ReportCreateRequest(periodStart);
        ReportInfoResponse response = new ReportInfoResponse(1L, organizationId, ReportType.WEEKLY, periodStart, periodStart.plusDays(6), null, AiSummaryStatus.PENDING, LocalDateTime.now(), List.of());

        given(reportCreateFacade.createWeeklyReport(accountUuid, periodStart))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/api/core/reports/weekly")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsBytes(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.organizationId").value(organizationId))
                .andExpect(jsonPath("$.data.reportType").value("WEEKLY"))
                .andExpect(jsonPath("$.data.aiSummaryStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.aiSummary").doesNotExist());
    }

    @Test
    @DisplayName("periodStart가 없으면 400을 반환한다")
    void createWeeklyReport_WhenPeriodStartIsNull_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/core/reports/weekly")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리포트 ID로 조회하면 리포트 정보를 반환한다.")
    void getReportById() throws Exception {
        // given
        UUID accountUuid = UUID.randomUUID();
        UserContext.setUserUuid(accountUuid);

        long reportId = 1L;
        long organizationId = 1L;
        LocalDate periodStart = LocalDate.of(2026, Month.AUGUST, 10);
        ReportInfoResponse response = new ReportInfoResponse(reportId, organizationId, ReportType.WEEKLY, periodStart, periodStart.plusDays(6), "AI Summary", AiSummaryStatus.COMPLETED, LocalDateTime.now(), List.of());

        given(reportGetUseCase.execute(accountUuid, reportId))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/core/reports/{report-id}", reportId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(reportId))
                .andExpect(jsonPath("$.data.organizationId").value(organizationId))
                .andExpect(jsonPath("$.data.reportType").value("WEEKLY"))
                .andExpect(jsonPath("$.data.aiSummaryStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.aiSummary").value("AI Summary"));
    }

    @Test
    @DisplayName("AI 요약 재시도 요청 시 갱신된 리포트 정보를 반환한다.")
    void retryAiSummary() throws Exception {
        // given
        UUID accountUuid = UUID.randomUUID();
        UserContext.setUserUuid(accountUuid);

        long reportId = 1L;
        long organizationId = 1L;
        LocalDate periodStart = LocalDate.of(2026, Month.AUGUST, 10);
        ReportInfoResponse response = new ReportInfoResponse(reportId, organizationId, ReportType.WEEKLY, periodStart, periodStart.plusDays(6), null, AiSummaryStatus.PENDING, LocalDateTime.now(), List.of());

        given(reportRetrySummaryUseCase.execute(accountUuid, reportId))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/api/core/reports/{report-id}/ai-summary/retry", reportId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(reportId))
                .andExpect(jsonPath("$.data.aiSummaryStatus").value("PENDING"));
    }

    @Test
    @DisplayName("존재하지 않는 리포트 ID로 조회하면 404를 반환한다.")
    void getReportById_WhenNotFound_ReturnsNotFound() throws Exception {
        // given
        UUID accountUuid = UUID.randomUUID();
        UserContext.setUserUuid(accountUuid);

        long reportId = 999L;

        given(reportGetUseCase.execute(accountUuid, reportId))
                .willThrow(new com.nhnacademy.inventory.reports.report.exception.ReportNotFoundException());

        // when
        ResultActions result = mockMvc.perform(get("/api/core/reports/{report-id}", reportId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("기간 시작일로 주간 리포트를 조회하면 리포트 정보를 반환한다.")
    void getWeeklyReportByPeriodStart() throws Exception {
        // given
        UUID accountUuid = UUID.randomUUID();
        UserContext.setUserUuid(accountUuid);

        long reportId = 1L;
        long organizationId = 1L;
        LocalDate periodStart = LocalDate.of(2026, Month.AUGUST, 10);
        ReportInfoResponse response = new ReportInfoResponse(reportId, organizationId, ReportType.WEEKLY, periodStart, periodStart.plusDays(6), "AI Summary", AiSummaryStatus.COMPLETED, LocalDateTime.now(), List.of());

        given(reportGetUseCase.getWeeklyReport(accountUuid, periodStart))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/core/reports/weekly")
                .param("periodStart", periodStart.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(reportId))
                .andExpect(jsonPath("$.data.organizationId").value(organizationId))
                .andExpect(jsonPath("$.data.reportType").value("WEEKLY"))
                .andExpect(jsonPath("$.data.aiSummaryStatus").value("COMPLETED"));
    }
}
