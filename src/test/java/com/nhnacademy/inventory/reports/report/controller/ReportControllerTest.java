package com.nhnacademy.inventory.reports.report.controller;

import com.nhnacademy.inventory.reports.report.domain.ReportType;
import com.nhnacademy.inventory.reports.report.dto.ReportCreateRequest;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.usecase.ReportCreateFacade;
import com.nhnacademy.inventory.support.SupportControllerTest;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest extends SupportControllerTest {

    @MockitoBean
    private ReportCreateFacade reportCreateFacade;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("주간 리포트를 생성하면 리포트 정보를 반환한다.")
    void createWeeklyReport() throws Exception {
        // given
        long organizationId = 1L;
        LocalDate periodStart = LocalDate.of(2026, Month.AUGUST, 10);
        ReportCreateRequest request = new ReportCreateRequest(periodStart);
        ReportInfoResponse response = new ReportInfoResponse(1L, organizationId, ReportType.WEEKLY, periodStart, periodStart.plusDays(6), null, LocalDateTime.now(), List.of());

        given(reportCreateFacade.createWeeklyReport(organizationId, periodStart))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/api/core/organizations/{organization-id}/reports/weekly", organizationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsBytes(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.organizationId").value(organizationId))
                .andExpect(jsonPath("$.data.reportType").value("WEEKLY"))
                .andExpect(jsonPath("$.data.aiSummary").doesNotExist());
    }

}