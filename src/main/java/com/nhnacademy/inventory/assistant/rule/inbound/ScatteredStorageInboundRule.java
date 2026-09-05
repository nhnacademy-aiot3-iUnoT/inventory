package com.nhnacademy.inventory.assistant.rule.inbound;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.FindingType;
import com.nhnacademy.inventory.assistant.rule.TargetReference;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import com.nhnacademy.inventory.assistant.dto.ZoneStock;
import com.nhnacademy.inventory.assistant.event.StockInboundCompletedEvent;
import com.nhnacademy.inventory.assistant.repository.AssistantStockRepository;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/*
    같은 저장소의 다른 구역에도 같은 품목이 있는지 확인
 */
@Component
@RequiredArgsConstructor
public class ScatteredStorageInboundRule implements InboundRule {

    private final AssistantStockRepository assistantStockRepository;
    private final MedicinePackageUnitRepository medicinePackageUnitRepository;
    private final ZoneRepository zoneRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Finding> evaluate(StockInboundCompletedEvent event, Long organizationId) {
        Zone zone = zoneRepository.findById(event.zoneId()).orElse(null);

        if (zone == null) {
            return Optional.empty();
        }

        Long storageId = zone.getStorage().getId();
        List<ZoneStock> others = assistantStockRepository.findStockInOtherZones(
                storageId, event.zoneId(), event.medicinePackageUnitId());

        if (others.isEmpty()) {
            return Optional.empty();
        }

        long total = others.stream().mapToLong(ZoneStock::quantity).sum();

        return Optional.of(new Finding(
                FindingType.SCATTERED_STORAGE,
                Severity.INFO,
                medicineName(event.medicinePackageUnitId()),
                "%s 입고 · %s에 %d개".formatted(zone.getName(), location(others), total),
                "같은 저장소의 다른 구역에도 보관되어 있습니다.",
                new TargetReference(TargetType.PACK_UNIT, storageId, event.medicinePackageUnitId())));
    }

    private String location(List<ZoneStock> others) {
        ZoneStock first = others.getFirst();

        if (others.size() == 1) {
            return first.zoneName();
        }

        return "%s 외 %d개 구역".formatted(first.zoneName(), others.size() - 1);
    }

    private String medicineName(Long medicinePackageUnitId) {
        return medicinePackageUnitRepository.findById(medicinePackageUnitId)
                .map(unit -> "%s / %s".formatted(unit.getMedicine().getProductName(), unit.getPackUnit()))
                .orElse("해당 의약품");
    }
}
