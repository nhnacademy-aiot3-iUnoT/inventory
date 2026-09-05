package com.nhnacademy.inventory.assistant.rule.outbound;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import com.nhnacademy.inventory.assistant.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.assistant.repository.AssistantStockRepository;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.FindingType;
import com.nhnacademy.inventory.assistant.rule.TargetReference;
import com.nhnacademy.inventory.inventories.threshold.domain.StockThreshold;
import com.nhnacademy.inventory.inventories.threshold.repository.StockThresholdRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/*
    출고 후 저장소 전체 재고가 사용자가 설정한 임계값 아래로 내려갔는지 확인
 */
@Component
@RequiredArgsConstructor
public class LowStockOutboundRule implements OutboundRule {

    private final AssistantStockRepository assistantStockRepository;
    private final StockThresholdRepository stockThresholdRepository;
    private final MedicinePackageUnitRepository medicinePackageUnitRepository;
    private final ZoneRepository zoneRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Finding> evaluate(StockOutboundCompletedEvent event, Long organizationId) {
        Zone zone = zoneRepository.findById(event.zoneId()).orElse(null);
        MedicinePackageUnit packUnit = medicinePackageUnitRepository
                .findById(event.medicinePackageUnitId())
                .orElse(null);

        if (zone == null || packUnit == null) {
            return Optional.empty();
        }

        Storage storage = zone.getStorage();
        StockThreshold threshold = stockThresholdRepository
                .findByStorageAndMedicinePackageUnit(storage, packUnit)
                .orElse(null);

        // 임계값이 존재하지 않으면 알리지 않음
        if (threshold == null || !threshold.getIsActive()) {
            return Optional.empty();
        }

        long total = assistantStockRepository.sumStorageQuantity(storage.getId(), packUnit.getId());

        if (total >= threshold.getThreshold()) {
            return Optional.empty();
        }

        return Optional.of(new Finding(
                FindingType.LOW_STOCK,
                total == 0 ? Severity.CRITICAL : Severity.WARN,
                "%s / %s".formatted(packUnit.getMedicine().getProductName(), packUnit.getPackUnit()),
                "%s · %d개 남음 · 임계값 %d개".formatted(storage.getName(), total, threshold.getThreshold()),
                explanation(total, threshold.getThreshold()),
                new TargetReference(TargetType.PACK_UNIT, storage.getId(), packUnit.getId())));
    }

    private String explanation(long total, int threshold) {
        if (total == 0) {
            return "재고가 모두 소진되었습니다.";
        }

        return "설정한 임계값 %d개 아래로 내려갔습니다.".formatted(threshold);
    }
}
