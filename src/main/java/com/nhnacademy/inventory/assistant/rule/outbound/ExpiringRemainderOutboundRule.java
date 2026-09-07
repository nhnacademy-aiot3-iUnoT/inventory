package com.nhnacademy.inventory.assistant.rule.outbound;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.FindingType;
import com.nhnacademy.inventory.assistant.rule.TargetReference;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import com.nhnacademy.inventory.assistant.dto.StockLot;
import com.nhnacademy.inventory.assistant.event.StockOutboundInspectionEvent;
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

// 출고하고 남은 재고 중 가장 빨리 만료되는 로트를 확인함
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
    public Optional<Finding> evaluate(StockOutboundInspectionEvent event, Long organizationId) {
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

        Zone zone = zoneRepository.findById(event.zoneId()).orElse(null);

        if (zone == null) {
            return Optional.empty();
        }

        return Optional.of(new Finding(
                FindingType.EXPIRING_STOCK,
                severityOf(daysLeft),
                medicineName(event.medicinePackageUnitId()),
                "%s · 로트 %s · %d개".formatted(zone.getName(), earliest.lotNumber(), earliest.quantity()),
                explanation(earliest, daysLeft),
                new TargetReference(TargetType.PACK_UNIT, zone.getStorage().getId(), event.medicinePackageUnitId())));
    }

    private Severity severityOf(long daysLeft) {
        if (daysLeft < 0) {
            return Severity.CRITICAL;
        }

        return daysLeft <= WARN_DAYS ? Severity.WARN : Severity.INFO;
    }

    private String explanation(StockLot earliest, long daysLeft) {
        String expiration = earliest.expirationDate().format(DATE_FORMAT);

        if (daysLeft < 0) {
            return "해당 재고가 %s에 만료되었습니다.".formatted(expiration);
        }

        return "해당 재고가 %s에 만료됩니다. 만료까지 %d일 남았습니다.".formatted(expiration, daysLeft);
    }

    private String medicineName(Long medicinePackageUnitId) {
        return medicinePackageUnitRepository.findById(medicinePackageUnitId)
                .map(unit -> "%s / %s".formatted(unit.getMedicine().getProductName(), unit.getPackUnit()))
                .orElse("해당 의약품");
    }
}
