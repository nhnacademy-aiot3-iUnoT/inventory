package com.nhnacademy.inventory.assistant.rule;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import com.nhnacademy.inventory.assistant.dto.StockLot;
import com.nhnacademy.inventory.assistant.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.assistant.repository.AssistantStockRepository;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExpiringRemainderOutboundRule implements OutboundRule {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long WARN_DAYS = 7;
    private static final long INFO_DAYS = 30;

    private final AssistantStockRepository assistantStockRepository;
    private final MedicinePackageUnitRepository medicinePackageUnitRepository;
    private final ZoneRepository zoneRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Finding> evaluate(StockOutboundCompletedEvent event, Long organizationId) {
        List<StockLot> remaining = assistantStockRepository
                .findRemainingLots(event.zoneId(), event.medicinePackageUnitId());

        if (remaining.isEmpty()) {
            return Optional.empty();
        }

        StockLot earliest = remaining.getFirst();
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), earliest.expirationDate());

        if (daysLeft > INFO_DAYS) {
            return Optional.empty();
        }

        return Optional.of(new Finding(
                FindingType.EXPIRING_STOCK,
                severityOf(daysLeft),
                describe(event, earliest, daysLeft),
                new TargetReference(TargetType.ZONE, event.zoneId())));
    }

    private Severity severityOf(long daysLeft) {
        if (daysLeft < 0) {
            return Severity.CRITICAL;
        }

        return daysLeft <= WARN_DAYS ? Severity.WARN : Severity.INFO;
    }

    private String describe(StockOutboundCompletedEvent event, StockLot earliest, long daysLeft) {
        String zoneName = zoneRepository.findById(event.zoneId())
                .map(Zone::getName)
                .orElseGet(() -> event.zoneId() + "구역");

        String medicineName = medicinePackageUnitRepository.findById(event.medicinePackageUnitId())
                .map(unit -> "%s / %s".formatted(unit.getMedicine().getProductName(), unit.getPackUnit()))
                .orElse("해당 의약품");

        String expiration = earliest.expirationDate().format(DATE_FORMAT);

        if (daysLeft < 0) {
            return "%s에 남은 %s 로트 %s(%d개)는 %s에 이미 만료되었습니다. 다음 출고 시 이 로트가 먼저 나가므로 폐기 처리하십시오."
                    .formatted(zoneName, medicineName, earliest.lotNumber(), earliest.quantity(), expiration);
        }

        return "%s에 남은 %s 로트 %s(%d개)의 유통기한이 %s로 %d일 남았습니다. 다음 출고 때 우선 소진하십시오."
                .formatted(zoneName, medicineName, earliest.lotNumber(), earliest.quantity(), expiration, daysLeft);
    }
}
