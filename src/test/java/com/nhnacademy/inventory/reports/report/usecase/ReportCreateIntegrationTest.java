package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicineRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportItemType;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.dto.ReportItemResponse;
import com.nhnacademy.inventory.reports.report.repository.ReportRepository;
import com.nhnacademy.inventory.support.TestFixtures;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest
class ReportCreateIntegrationTest {

    private static final LocalDate PERIOD_START = LocalDate.of(2026, Month.AUGUST, 10);

    @Autowired
    private ReportCreateFacade reportCreateFacade;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private MedicinePackageUnitRepository medicinePackageUnitRepository;

    private long organizationId;

    @BeforeEach
    void setUp() {
        Organization organization = organizationRepository.save(Organization.create("1000010000", "테스트"));
        organizationId = organization.getId();

        OrganizationMember member = TestFixtures.createOrganizationMember(organization);
        UUID accountUuid = member.getAccountUuid();
        organizationMemberRepository.save(member);

        Medicine medicine = medicineRepository.save(TestFixtures.createMedicine("A001", "타이레놀정"));
        medicinePackageUnitRepository.save(TestFixtures.createPackageUnit(medicine, "10정"));

        UserContext.setUserUuid(accountUuid);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();

        // report_items 가 reports 를 참조하므로 벌크 삭제 대신 엔티티 단위로 삭제
        reportRepository.deleteAll();
        medicinePackageUnitRepository.deleteAllInBatch();
        medicineRepository.deleteAllInBatch();
        organizationMemberRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("리포트를 생성하면 의약품 항목이 함께 저장된다")
    void createWeeklyReport_SavesReportItems() {
        // when
        ReportInfoResponse response = reportCreateFacade.createWeeklyReport(organizationId, PERIOD_START);

        // then
        assertThat(response.periodStart())
                .isEqualTo(PERIOD_START);
        assertThat(response.periodEnd())
                .isEqualTo(PERIOD_START.plusDays(6));

        assertThat(response.items())
                .isNotEmpty()
                .allSatisfy(item -> {
                    assertThat(item.medicineName())
                            .isEqualTo("타이레놀정");
                    assertThat(item.packUnit())
                            .isEqualTo("10정");
                    assertThat(item.quantity())
                            .isPositive();
                });

        assertThat(response.items())
                .extracting(ReportItemResponse::reportItemType)
                .contains(ReportItemType.USAGE);

        // 저장까지 실제로 됐는지 (cascade 확인)
        Report saved = reportRepository.findByIdWithItems(response.reportId()).orElseThrow();
        assertThat(saved.getReportItems()).hasSameSizeAs(response.items());
    }

    @Test
    @DisplayName("이미 생성된 기간을 다시 요청하면 기존 리포트를 그대로 반환한다")
    void createWeeklyReport_WhenAlreadyExists_ReturnsSameReport() {
        // given
        ReportInfoResponse first = reportCreateFacade.createWeeklyReport(organizationId, PERIOD_START);

        // when
        ReportInfoResponse second = reportCreateFacade.createWeeklyReport(organizationId, PERIOD_START);

        // then
        assertThat(second.reportId())
                .isEqualTo(first.reportId());
        assertThat(reportRepository.count())
                .isEqualTo(1);
    }

    @Test
    @Disabled("실제 GOOGLE_API_KEY 필요 - 수동 확인용")
    @DisplayName("리포트 생성 후 AI 요약이 비동기로 채워진다")
    void createWeeklyReport_FillsAiSummaryAsynchronously() {
        // when
        ReportInfoResponse response = reportCreateFacade.createWeeklyReport(organizationId, PERIOD_START);

        // then: 응답 시점에는 아직 요약이 없음
        assertThat(response.aiSummary())
                .isNull();

        // 잠시 뒤 채워진다
        await().atMost(30, SECONDS)
                .pollInterval(1, SECONDS)
                .untilAsserted(() -> {
                    Report saved = reportRepository.findById(response.reportId()).orElseThrow();

                    assertThat(saved.getAiSummary())
                            .isNotBlank();
                    log.info("생성된 AI 요약:\n{}", saved.getAiSummary());
                });
    }
}
