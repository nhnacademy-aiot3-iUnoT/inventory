package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.repository.ReportRepository;
import com.nhnacademy.inventory.reports.report.service.ReportSummaryService;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReportCreateConcurrencyIntegrationTest {

    @Autowired
    private ReportCreateFacade reportCreateFacade;

    @MockitoBean
    private ReportSummaryService reportSummaryService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    private ExecutorService threadPool;

    private long organizationId;

    private UUID accountUuid;

    @BeforeEach
    void setUp() {
        Organization organization = organizationRepository.save(Organization.create("1000010000", "테스트"));
        organizationId = organization.getId();
        OrganizationMember member = TestFixtures.createOrganizationMember(organization);
        accountUuid = member.getAccountUuid();
        organizationMemberRepository.save(member);
    }

    @AfterEach
    void tearDown() {
        reportRepository.deleteAllInBatch();
        organizationMemberRepository.deleteAllInBatch();
        organizationRepository.deleteAllInBatch();

        if (threadPool != null) {
            threadPool.shutdown();
        }
    }

    @RepeatedTest(10)
    @DisplayName("동시에 리포트를 생성해도, 리포트는 한 번만 생성된다.")
    void execute_WhenCreateConcurrency_ReportCreatesOnce() throws Exception {
        // given
        int threadCount = 16;
        threadPool = Executors.newFixedThreadPool(threadCount);

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<ReportInfoResponse>> futures = new ArrayList<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            futures.add(threadPool.submit(() -> {
                UserContext.setUserUuid(accountUuid);

                ready.countDown();

                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                try {
                    return reportCreateFacade.createWeeklyReport(organizationId, LocalDate.of(2026, Month.AUGUST, 10));
                } finally {
                    UserContext.clear();
                }
            }));
        }

        ready.await();
        start.countDown();

        long reportId = futures.getFirst().get().reportId();

        // then
        for (Future<ReportInfoResponse> future : futures) {
            ReportInfoResponse response = future.get();

            assertThat(response)
                    .isNotNull();
            assertThat(response.reportId())
                    .isEqualTo(reportId);
        }

        assertThat(reportRepository.count())
                .isEqualTo(1);
    }
}