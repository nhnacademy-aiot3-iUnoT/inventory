package com.nhnacademy.inventory.reports.report.controller;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.reports.report.domain.AiSummaryStatus;
import com.nhnacademy.inventory.reports.report.domain.ReportItemType;
import com.nhnacademy.inventory.reports.report.domain.ReportType;
import com.nhnacademy.inventory.reports.report.dto.ReportCreateRequest;
import com.nhnacademy.inventory.reports.report.dto.ReportDoorResponse;
import com.nhnacademy.inventory.reports.report.dto.ReportEnvironmentResponse;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.dto.ReportItemResponse;
import com.nhnacademy.inventory.reports.report.exception.ReportNotFoundException;
import com.nhnacademy.inventory.reports.report.usecase.ReportCreateFacade;
import com.nhnacademy.inventory.reports.report.usecase.ReportGetUseCase;
import com.nhnacademy.inventory.reports.report.usecase.ReportRecreateUseCase;
import com.nhnacademy.inventory.reports.report.usecase.ReportRetrySummaryUseCase;
import com.nhnacademy.inventory.support.RestDocsUtils;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest extends SupportControllerTest {

    private static final long STORAGE_ID = 1L;
    private static final long ORGANIZATION_ID = 1L;
    private static final long REPORT_ID = 1L;
    private static final LocalDate PERIOD_START = LocalDate.of(2026, Month.AUGUST, 10);
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, Month.AUGUST, 17, 9, 0);

    @MockitoBean
    private ReportCreateFacade reportCreateFacade;

    @MockitoBean
    private ReportGetUseCase reportGetUseCase;

    @MockitoBean
    private ReportRecreateUseCase reportRecreateUseCase;

    @MockitoBean
    private ReportRetrySummaryUseCase reportRetrySummaryUseCase;

    @Autowired
    private JsonMapper jsonMapper;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("저장소 ID 및 기간 시작일로 주간 리포트를 조회하면 리포트 정보를 반환한다.")
    void getWeeklyReportByPeriodStart() throws Exception {
        // given
        login();
        given(reportGetUseCase.getWeeklyReportByPeriod(STORAGE_ID, PERIOD_START))
                .willReturn(Optional.of(emptyReport("AI 요약", AiSummaryStatus.COMPLETED)));

        // when
        ResultActions result = mockMvc.perform(get("/api/core/storages/{storage-id}/reports/weekly", STORAGE_ID)
                .param("periodStart", PERIOD_START.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(REPORT_ID))
                .andExpect(jsonPath("$.data.reportType").value("WEEKLY"))
                .andExpect(jsonPath("$.data.aiSummaryStatus").value("COMPLETED"))
                .andDo(document("report-get-weekly",
                        pathParameters(
                                parameterWithName("storage-id").description("저장소 ID")
                        ),
                        queryParameters(
                                parameterWithName("periodStart").description("주간 시작일(월요일, yyyy-MM-dd)")
                        ),
                        responseFields(summaryResponseFields())
                ));
    }

    @Test
    @DisplayName("리포트가 없어도 200과 함께 null 데이터를 반환한다.")
    void getWeeklyReport_WhenNotExists_ReturnsNullData() throws Exception {
        // given
        login();
        given(reportGetUseCase.getWeeklyReportByPeriod(STORAGE_ID, PERIOD_START))
                .willReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/api/core/storages/{storage-id}/reports/weekly", STORAGE_ID)
                        .param("periodStart", PERIOD_START.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("주간 리포트를 생성하면 리포트 정보를 반환한다.")
    void createWeeklyReport() throws Exception {
        // given
        login();
        ReportCreateRequest request = new ReportCreateRequest(PERIOD_START);

        given(reportCreateFacade.createWeeklyReport(STORAGE_ID, PERIOD_START))
                .willReturn(emptyReport(null, AiSummaryStatus.PENDING));

        // when
        ResultActions result = mockMvc.perform(post("/api/core/storages/{storage-id}/reports/weekly", STORAGE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsBytes(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.organizationId").value(ORGANIZATION_ID))
                .andExpect(jsonPath("$.data.aiSummaryStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.aiSummary").doesNotExist())
                .andDo(document("report-create-weekly",
                        pathParameters(
                                parameterWithName("storage-id").description("저장소 ID")
                        ),
                        requestFields(
                                fieldWithPath("periodStart")
                                        .description("주간 시작일. 지난 주 이전의 월요일만 허용한다.")
                        ),
                        responseFields(summaryResponseFields())
                ));
    }

    @Test
    @DisplayName("periodStart가 없으면 400을 반환한다")
    void createWeeklyReport_WhenPeriodStartIsNull_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/core/storages/{storage-id}/reports/weekly", STORAGE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리포트 ID로 조회하면 아이템·환경·문 개폐까지 포함한 정보를 반환한다.")
    void getReportById() throws Exception {
        // given
        login();
        given(reportGetUseCase.getWeeklyReportById(STORAGE_ID, REPORT_ID))
                .willReturn(detailedReport());

        // when
        ResultActions result = mockMvc.perform(
                get("/api/core/storages/{storage-id}/reports/{report-id}", STORAGE_ID, REPORT_ID)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(REPORT_ID))
                .andExpect(jsonPath("$.data.aiSummaryStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.items[0].medicineName").value("타이레놀정 500mg"))
                .andExpect(jsonPath("$.data.environments[0].sensorType").value("temperature"))
                .andExpect(jsonPath("$.data.doors[0].totalOpenCount").value(53))
                .andDo(document("report-get-detail",
                        pathParameters(
                                parameterWithName("storage-id").description("저장소 ID"),
                                parameterWithName("report-id").description("리포트 ID")
                        ),
                        responseFields(detailResponseFields())
                ));
    }

    @Test
    @DisplayName("존재하지 않는 리포트 ID로 조회하면 404를 반환한다.")
    void getReportById_WhenNotFound_ReturnsNotFound() throws Exception {
        // given
        login();
        given(reportGetUseCase.getWeeklyReportById(STORAGE_ID, 999L))
                .willThrow(new ReportNotFoundException());

        // when & then
        mockMvc.perform(get("/api/core/storages/{storage-id}/reports/{report-id}", STORAGE_ID, 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("리포트를 재생성하면 초기화된 리포트 정보를 반환한다.")
    void recreateReport() throws Exception {
        // given
        login();
        given(reportRecreateUseCase.recreateWeekly(STORAGE_ID, REPORT_ID))
                .willReturn(emptyReport(null, AiSummaryStatus.PENDING));

        // when
        ResultActions result = mockMvc.perform(
                post("/api/core/storages/{storage-id}/reports/{report-id}/recreations", STORAGE_ID, REPORT_ID));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").value(REPORT_ID))
                .andExpect(jsonPath("$.data.aiSummaryStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.aiSummary").doesNotExist())
                .andDo(document("report-recreate",
                        pathParameters(
                                parameterWithName("storage-id").description("저장소 ID"),
                                parameterWithName("report-id").description("리포트 ID")
                        ),
                        responseFields(summaryResponseFields())
                ));
    }

    @Test
    @DisplayName("AI 요약 재생성 요청 시 204를 반환한다.")
    void retryAiSummary() throws Exception {
        // given
        login();

        // when
        ResultActions result = mockMvc.perform(
                post("/api/core/storages/{storage-id}/reports/{report-id}/ai-summary/recreations",
                        STORAGE_ID, REPORT_ID));

        // then
        result.andExpect(status().isNoContent())
                .andDo(document("report-retry-ai-summary",
                        pathParameters(
                                parameterWithName("storage-id").description("저장소 ID"),
                                parameterWithName("report-id").description("리포트 ID")
                        )
                ));
    }

    private void login() {
        UserContext.setUserUuid(UUID.randomUUID());
    }

    private ReportInfoResponse emptyReport(String aiSummary, AiSummaryStatus status) {
        return new ReportInfoResponse(
                REPORT_ID, ORGANIZATION_ID, STORAGE_ID, ReportType.WEEKLY,
                PERIOD_START, PERIOD_START.plusDays(6),
                aiSummary, status, CREATED_AT,
                List.of(), List.of(), List.of());
    }

    private ReportInfoResponse detailedReport() {
        ReportItemResponse item = new ReportItemResponse(
                1L, ReportItemType.OUTBOUND, 10L, "타이레놀정 500mg", "10정(PTP)", 1240);

        // 문서에 실리는 예시라 집계값과 일별 추이가 서로 맞아떨어지게 맞춰둔다.
        // 3일차만 임계 상한(8.0)을 넘어 outOfRangeDays 가 1이다.
        ReportEnvironmentResponse environment = new ReportEnvironmentResponse(
                29L, "temperature", "C",
                new BigDecimal("5.3"), new BigDecimal("3.2"), new BigDecimal("9.9"),
                new BigDecimal("2.0"), new BigDecimal("8.0"),
                1, 7,
                List.of(
                        environmentPoint(0, "5.1", "3.6", "6.8"),
                        environmentPoint(1, "4.8", "3.2", "6.1"),
                        environmentPoint(2, "6.9", "5.0", "9.9"),
                        environmentPoint(3, "5.4", "4.1", "7.2"),
                        environmentPoint(4, "5.0", "3.8", "6.5"),
                        environmentPoint(5, "4.9", "3.5", "6.2"),
                        environmentPoint(6, "5.0", "3.7", "6.4")));

        ReportDoorResponse door = new ReportDoorResponse(
                29L, 53L, 185L, 7,
                List.of(
                        doorPoint(0, 9L, 31L),
                        doorPoint(1, 6L, 20L),
                        doorPoint(2, 11L, 38L),
                        doorPoint(3, 8L, 27L),
                        doorPoint(4, 7L, 24L),
                        doorPoint(5, 5L, 17L),
                        doorPoint(6, 7L, 28L)));

        return new ReportInfoResponse(
                REPORT_ID, ORGANIZATION_ID, STORAGE_ID, ReportType.WEEKLY,
                PERIOD_START, PERIOD_START.plusDays(6),
                "AI 요약", AiSummaryStatus.COMPLETED, CREATED_AT,
                List.of(item), List.of(environment), List.of(door));
    }

    private ReportEnvironmentResponse.DailyPointResponse environmentPoint(int dayOffset, String avg, String min, String max) {
        return new ReportEnvironmentResponse.DailyPointResponse(
                PERIOD_START.plusDays(dayOffset), new BigDecimal(avg), new BigDecimal(min), new BigDecimal(max));
    }

    private ReportDoorResponse.DailyPointResponse doorPoint(int dayOffset, long openCount, long openMinutes) {
        return new ReportDoorResponse.DailyPointResponse(PERIOD_START.plusDays(dayOffset), openCount, openMinutes);
    }

    /** 아이템·환경·문 목록이 비어 있는 응답용. 배열 안쪽은 문서화하지 않는다. */
    private List<FieldDescriptor> summaryResponseFields() {
        List<FieldDescriptor> fields = new ArrayList<>(RestDocsUtils.successResponseFields());
        fields.addAll(reportScalarFields());
        fields.add(fieldWithPath("data.items").type(JsonFieldType.ARRAY).description("입고·사용·폐기 집계"));
        fields.add(fieldWithPath("data.environments").type(JsonFieldType.ARRAY).description("구역·센서별 환경 요약"));
        fields.add(fieldWithPath("data.doors").type(JsonFieldType.ARRAY).description("구역별 문 개폐 요약"));

        return fields;
    }

    private List<FieldDescriptor> detailResponseFields() {
        List<FieldDescriptor> fields = new ArrayList<>(RestDocsUtils.successResponseFields());
        fields.addAll(reportScalarFields());

        fields.add(fieldWithPath("data.items[].reportItemId").description("리포트 아이템 ID"));
        fields.add(fieldWithPath("data.items[].reportItemType").description("INBOUND, OUTBOUND, DISPOSAL"));
        fields.add(fieldWithPath("data.items[].medicinePackageUnitId").description("의약품 포장 단위 ID"));
        fields.add(fieldWithPath("data.items[].medicineName").description("의약품명"));
        fields.add(fieldWithPath("data.items[].packUnit").description("포장 단위"));
        fields.add(fieldWithPath("data.items[].quantity").description("수량"));

        fields.add(fieldWithPath("data.environments[].zoneId").description("구역 ID"));
        fields.add(fieldWithPath("data.environments[].sensorType").description("센서 종류 (temperature, humidity 등)"));
        fields.add(fieldWithPath("data.environments[].unit").type(JsonFieldType.STRING).description("단위").optional());
        fields.add(fieldWithPath("data.environments[].avgValue").description("기간 평균"));
        fields.add(fieldWithPath("data.environments[].minValue").description("기간 최저"));
        fields.add(fieldWithPath("data.environments[].maxValue").description("기간 최고"));
        fields.add(fieldWithPath("data.environments[].thresholdMin").type(JsonFieldType.NUMBER).description("임계 하한. 미설정이면 null").optional());
        fields.add(fieldWithPath("data.environments[].thresholdMax").type(JsonFieldType.NUMBER).description("임계 상한. 미설정이면 null").optional());
        fields.add(fieldWithPath("data.environments[].outOfRangeDays").description("임계를 벗어난 일수"));
        fields.add(fieldWithPath("data.environments[].measuredDays").description("측정된 일수"));
        fields.add(fieldWithPath("data.environments[].dailyPoints[].date").description("일자"));
        fields.add(fieldWithPath("data.environments[].dailyPoints[].avgValue").description("그날 평균"));
        fields.add(fieldWithPath("data.environments[].dailyPoints[].minValue").description("그날 최저"));
        fields.add(fieldWithPath("data.environments[].dailyPoints[].maxValue").description("그날 최고"));

        fields.add(fieldWithPath("data.doors[].zoneId").description("구역 ID"));
        fields.add(fieldWithPath("data.doors[].totalOpenCount").description("기간 총 개폐 횟수"));
        fields.add(fieldWithPath("data.doors[].totalOpenMinutes").description("기간 누적 개방 시간(분)"));
        fields.add(fieldWithPath("data.doors[].measuredDays").description("측정된 일수"));
        fields.add(fieldWithPath("data.doors[].dailyPoints[].date").description("일자"));
        fields.add(fieldWithPath("data.doors[].dailyPoints[].openCount").description("그날 개폐 횟수"));
        fields.add(fieldWithPath("data.doors[].dailyPoints[].openMinutes").description("그날 누적 개방 시간(분)"));

        return fields;
    }

    private List<FieldDescriptor> reportScalarFields() {
        return List.of(
                fieldWithPath("data.reportId").description("리포트 ID"),
                fieldWithPath("data.organizationId").description("조직 ID"),
                fieldWithPath("data.storageId").description("저장소 ID"),
                fieldWithPath("data.reportType").description("리포트 종류 (WEEKLY)"),
                fieldWithPath("data.periodStart").description("집계 시작일"),
                fieldWithPath("data.periodEnd").description("집계 종료일"),
                fieldWithPath("data.aiSummary").type(JsonFieldType.STRING)
                        .description("AI 요약. 생성 전이면 null").optional(),
                fieldWithPath("data.aiSummaryStatus")
                        .description("PENDING(대기), PROGRESS(생성 중), COMPLETED(완료), FAILED(실패)"),
                fieldWithPath("data.createdAt").description("리포트 생성 시각")
        );
    }
}
