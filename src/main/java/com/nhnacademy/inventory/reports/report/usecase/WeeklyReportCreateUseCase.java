package com.nhnacademy.inventory.reports.report.usecase;

import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.reports.report.domain.Report;
import com.nhnacademy.inventory.reports.report.domain.ReportType;
import com.nhnacademy.inventory.reports.report.dto.ReportCreatedEvent;
import com.nhnacademy.inventory.reports.report.dto.ReportInfoResponse;
import com.nhnacademy.inventory.reports.report.service.OrganizationMemberValidator;
import com.nhnacademy.inventory.reports.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyReportCreateUseCase {

    private final ReportService reportService;
    private final OrganizationMemberValidator memberValidator;
    private final ApplicationEventPublisher eventPublisher;

    // 더미 생성용 임시 의존성
    private final MedicinePackageUnitRepository medicinePackageUnitRepository;

    // 주간 리포트를 생성한다. 이미 주간 리포트가 있으면 그걸 반환하고, 없으면 생성한다.
    @Transactional
    public ReportInfoResponse execute(Long organizationId, LocalDate periodStart) {
        memberValidator.validate(organizationId);

        Report report = reportService.find(organizationId, ReportType.WEEKLY, periodStart)
                .orElseGet(() -> generate(organizationId, periodStart));

        return ReportInfoResponse.of(report);
    }

    private Report generate(Long organizationId, LocalDate periodStart) {
        Report report = Report.weeklyOf(organizationId, periodStart);

        Report saved = reportService.register(report);

        collectReportItems(saved);

        eventPublisher.publishEvent(new ReportCreatedEvent(saved.getId()));

        return saved;
    }

    /**
     * TODO: 더미값 -> StockTransaction 집계로 교체
     *  - 사용량: transactionType == OUTBOUND (TRANSFER_OUT, INFO_CORRECTION_OUT 은 실제 소비가 아니므로 제외함)
     *  - 폐기량: transactionType == DISPOSAL
     *  - 기간: processedAt이 report.getPeriodStart() ~ report.getPeriodEnd() 사이인 경우
     *  - 조직: zone.storage.organization.id == report.getOrganizationId()
     */
    private void collectReportItems(Report report) {
        List<MedicinePackageUnit> units = medicinePackageUnitRepository.findAll().stream()
                .limit(6)
                .toList();

        int[] usageQuantities = {320, 45, 30, 22, 15, 7};
        int[] disposalQuantities = {24, 3, 1, 0, 0, 3};

        for (int i = 0; i < units.size(); i++) {
            report.addUsage(units.get(i), usageQuantities[i]);

            if (disposalQuantities[i] > 0) {
                report.addDisposal(units.get(i), disposalQuantities[i]);
            }
        }
    }
}
