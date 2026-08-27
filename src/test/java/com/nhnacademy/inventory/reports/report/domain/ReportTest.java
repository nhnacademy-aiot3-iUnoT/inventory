package com.nhnacademy.inventory.reports.report.domain;

import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.reports.report.exception.InvalidWeeklyPeriodException;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ReportTest {

    @Test
    @DisplayName("주간 리포트 생성일이 월요일이 아니라면 InvalidWeeklyPeriodException이 발생한다.")
    void weeklyOf_WhenPeriodStartIsNotMonday_ThrowsException() {
        // given
        long organizationId = 1L;
        long storageId = 1L;
        LocalDate periodStart = mock(LocalDate.class);

        given(periodStart.getDayOfWeek())
                .willReturn(DayOfWeek.WEDNESDAY);

        // when & then
        assertThatThrownBy(() -> Report.weeklyOf(organizationId, storageId, periodStart))
                .isInstanceOf(InvalidWeeklyPeriodException.class);
    }

    @Test
    @DisplayName("주간 리포트 생성일이 월요일이라면 엔티티가 생성된다.")
    void weeklyOf_WhenPeriodStartIsMonday_CreatesEntity() {
        // given
        long organizationId = 1L;
        long storageId = 1L;
        LocalDate periodStart = mock(LocalDate.class);

        given(periodStart.getDayOfWeek())
                .willReturn(DayOfWeek.MONDAY);

        // when
        Report report = Report.weeklyOf(organizationId, storageId, periodStart);

        // then
        assertThat(report.getOrganizationId())
                .isEqualTo(organizationId);
        assertThat(report.getStorageId())
                .isEqualTo(storageId);
    }

    @Test
    @DisplayName("입고 항목을 추가하면 의약품 정보와 INBOUND 타입이 스냅샷으로 저장된다.")
    void addInbound_SnapshotsMedicineInfo() {
        // given
        Report report = Report.weeklyOf(1L, 1L, LocalDate.of(2026, Month.AUGUST, 10));
        MedicinePackageUnit unit = TestFixtures.createPackageUnit(
                TestFixtures.createMedicine("202106092", "타이레놀정500밀리그람(아세트아미노펜)")
        );
        int quantity = 100;

        // when
        report.addInbound(unit, quantity);

        ReportItem item = report.getReportItems().getFirst();

        // then
        assertThat(item)
                .isNotNull();
        assertThat(item.getReportItemType())
                .isEqualTo(ReportItemType.INBOUND);
        assertThat(item.getQuantity())
                .isEqualTo(quantity);
        assertThat(item.getPackUnit())
                .isEqualTo(unit.getPackUnit());
        assertThat(item.getMedicineName())
                .isEqualTo(unit.getMedicine().getProductName());
    }

    @Test
    @DisplayName("사용(출고) 항목을 추가하면 의약품 정보와 OUTBOUND 타입이 스냅샷으로 저장된다.")
    void addOutbound_SnapshotsMedicineInfo() {
        // given
        Report report = Report.weeklyOf(1L, 1L, LocalDate.of(2026, Month.AUGUST, 10));
        MedicinePackageUnit unit = TestFixtures.createPackageUnit(
                TestFixtures.createMedicine("202106092", "타이레놀정500밀리그람(아세트아미노펜)")
        );
        int quantity = 50;

        // when
        report.addOutbound(unit, quantity);

        ReportItem item = report.getReportItems().getFirst();

        // then
        assertThat(item)
                .isNotNull();
        assertThat(item.getReportItemType()).isEqualTo(ReportItemType.OUTBOUND);
        assertThat(item.getQuantity())
                .isEqualTo(quantity);
        assertThat(item.getPackUnit())
                .isEqualTo(unit.getPackUnit());
        assertThat(item.getMedicineName())
                .isEqualTo(unit.getMedicine().getProductName());
    }

    @Test
    @DisplayName("폐기 항목을 추가하면 의약품 정보와 DISPOSAL 타입이 스냅샷으로 저장된다.")
    void addDisposal_SnapshotsMedicineInfo() {
        // given
        Report report = Report.weeklyOf(1L, 1L, LocalDate.of(2026, Month.AUGUST, 10));
        MedicinePackageUnit unit = TestFixtures.createPackageUnit(
                TestFixtures.createMedicine("202106092", "타이레놀정500밀리그람(아세트아미노펜)")
        );
        int quantity = 3;

        // when
        report.addDisposal(unit, quantity);

        ReportItem item = report.getReportItems().getFirst();

        // then
        assertThat(item)
                .isNotNull();
        assertThat(item.getReportItemType())
                .isEqualTo(ReportItemType.DISPOSAL);
        assertThat(item.getQuantity())
                .isEqualTo(quantity);
        assertThat(item.getPackUnit())
                .isEqualTo(unit.getPackUnit());
        assertThat(item.getMedicineName())
                .isEqualTo(unit.getMedicine().getProductName());
    }

    @Test
    @DisplayName("리포트 생성 시 aiSummaryStatus는 PENDING이고 상태 전이가 올바르게 동작한다.")
    void aiSummaryStatus_TransitionsCorrectly() {
        // given
        Report report = Report.weeklyOf(1L, 1L, LocalDate.of(2026, Month.AUGUST, 10));

        // init -> PENDING
        assertThat(report.getAiSummaryStatus())
                .isEqualTo(AiSummaryStatus.PENDING);

        // updateSummary -> COMPLETED
        report.updateSummary("AI 요약 완료");
        assertThat(report.getAiSummaryStatus())
                .isEqualTo(AiSummaryStatus.COMPLETED);
        assertThat(report.getAiSummary())
                .isEqualTo("AI 요약 완료");

        // failSummary -> FAILED
        report.failSummary();
        assertThat(report.getAiSummaryStatus())
                .isEqualTo(AiSummaryStatus.FAILED);
    }
}