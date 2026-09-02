package com.nhnacademy.inventory.assistant.rule;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import com.nhnacademy.inventory.assistant.dto.StockLot;
import com.nhnacademy.inventory.assistant.event.StockInboundCompletedEvent;
import com.nhnacademy.inventory.assistant.repository.AssistantStockRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EarlierExpiryInboundRule implements InboundRule {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int LOT_LIST_LIMIT = 2;

    private final AssistantStockRepository assistantStockRepository;
    private final MedicinePackageUnitRepository medicinePackageUnitRepository;
    private final ZoneRepository zoneRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Finding> evaluate(StockInboundCompletedEvent event, Long organizationId) {
        List<StockLot> lots = assistantStockRepository.findEarlierExpiryLots(
                event.zoneId(), event.medicinePackageUnitId(), event.expirationDate());

        if (lots.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Finding(
                FindingType.EXPIRY_ORDER,
                Severity.WARN,
                describe(event, lots),
                new TargetReference(TargetType.ZONE, event.zoneId())));
    }

    private String describe(StockInboundCompletedEvent event, List<StockLot> lots) {
        String zoneName = zoneRepository.findById(event.zoneId())
                .map(Zone::getName)
                .orElseGet(() -> event.zoneId() + "구역");

        String medicineName = medicinePackageUnitRepository.findById(event.medicinePackageUnitId())
                .map(this::toMedicineName)
                .orElse("해당 의약품");

        int total = lots.stream().mapToInt(StockLot::quantity).sum();
        StockLot earliest = lots.getFirst();

        return "%s에 %s 재고 %d개가 남아 있습니다. 가장 빠른 유통기한은 %s(%s)로 이번 입고분 %s보다 앞섭니다. 이 재고를 먼저 출고하십시오."
                .formatted(
                        zoneName,
                        medicineName,
                        total,
                        earliest.expirationDate().format(DATE_FORMAT),
                        lotLabel(lots),
                        event.expirationDate().format(DATE_FORMAT));
    }

    private String toMedicineName(MedicinePackageUnit packageUnit) {
        return "%s / %s".formatted(packageUnit.getMedicine().getProductName(), packageUnit.getPackUnit());
    }

    private String lotLabel(List<StockLot> lots) {
        if (lots.size() > LOT_LIST_LIMIT) {
            return "로트 %d건".formatted(lots.size());
        }

        return lots.stream()
                .map(lot -> "로트 " + lot.lotNumber())
                .collect(Collectors.joining(", "));
    }
}
