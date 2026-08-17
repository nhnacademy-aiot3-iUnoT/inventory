package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.repository.StockTransactionRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicineRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest
class ReportCreateIntegrationTest {

    // StockTransaction의 processedAt이 @PrePersist로 now()가 되므로,
    // 픽스처가 집계 대상에 들어오도록 '이번 주 월요일'을 기간 시작으로 사용한다.
    private static final LocalDate PERIOD_START = LocalDate.now().with(DayOfWeek.MONDAY);

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

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    private UUID accountUuid;

    @BeforeEach
    void setUp() {
        Organization organization = organizationRepository.save(Organization.create("1000010000", "테스트"));

        OrganizationMember member = TestFixtures.createOrganizationMember(organization);
        accountUuid = member.getAccountUuid();
        organizationMemberRepository.save(member);

        Medicine medicine = medicineRepository.save(TestFixtures.createMedicine("A001", "타이레놀정"));
        MedicinePackageUnit packageUnit = medicinePackageUnitRepository.save(TestFixtures.createPackageUnit(medicine, "10정"));

        Storage storage = storageRepository.save(TestFixtures.createStorage(organization));
        Zone zone = zoneRepository.save(TestFixtures.createZone(storage));

        stockTransactionRepository.save(
                TestFixtures.createStockTransaction(packageUnit, zone, TransactionType.OUTBOUND, 30));
        stockTransactionRepository.save(
                TestFixtures.createStockTransaction(packageUnit, zone, TransactionType.OUTBOUND, 12));
        stockTransactionRepository.save(
                TestFixtures.createStockTransaction(packageUnit, zone, TransactionType.DISPOSAL, 5));

        UserContext.setUserUuid(accountUuid);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();

        // report_items 가 reports 를 참조하므로 벌크 삭제 대신 엔티티 단위로 삭제
        reportRepository.deleteAll();
        stockTransactionRepository.deleteAllInBatch();
        zoneRepository.deleteAllInBatch();
        storageRepository.deleteAllInBatch();
        medicinePackageUnitRepository.deleteAllInBatch();
        medicineRepository.deleteAllInBatch();
        organizationMemberRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("리포트를 생성하면 의약품 항목이 함께 저장된다")
    void createWeeklyReport_SavesReportItems() {
        // when
        ReportInfoResponse response = reportCreateFacade.createWeeklyReport(accountUuid, PERIOD_START);

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
                });
        assertThat(response.items())
                .filteredOn(item -> item.reportItemType() == ReportItemType.USAGE)
                .singleElement()
                .satisfies(item -> assertThat(item.quantity()).isEqualTo(42));
        assertThat(response.items())
                .extracting(ReportItemResponse::reportItemType)
                .contains(ReportItemType.USAGE);

        // 저장까지 실제로 됐는지 (cascade 확인)
        Report saved = reportRepository.findByIdWithItems(response.reportId())
                .orElseThrow();
        assertThat(saved.getReportItems())
                .hasSameSizeAs(response.items());
    }

    @Test
    @DisplayName("이미 생성된 기간을 다시 요청하면 기존 리포트를 그대로 반환한다")
    void createWeeklyReport_WhenAlreadyExists_ReturnsSameReport() {
        // given
        ReportInfoResponse first = reportCreateFacade.createWeeklyReport(accountUuid, PERIOD_START);

        // when
        ReportInfoResponse second = reportCreateFacade.createWeeklyReport(accountUuid, PERIOD_START);

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
        ReportInfoResponse response = reportCreateFacade.createWeeklyReport(accountUuid, PERIOD_START);

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
