package com.nhnacademy.inventory.assistant.rule.inbound;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import com.nhnacademy.inventory.assistant.dto.EnvRange;
import com.nhnacademy.inventory.assistant.event.StockInboundCompletedEvent;
import com.nhnacademy.inventory.assistant.repository.AssistantEnvironmentRepository;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.FindingType;
import com.nhnacademy.inventory.assistant.rule.TargetReference;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// 입고한 구역의 환경 기준이 의약품 보관 기준을 벗어날 수 있는지 확인함 (EnvironmentReview 는 이탈 후 검토)
@Component
@RequiredArgsConstructor
public class StorageConditionInboundRule implements InboundRule {

    private static final Map<String, String> TYPE_LABEL = Map.of(
            "TEMPERATURE", "온도",
            "HUMIDITY", "습도",
            "ILLUMINANCE", "조도");

    private final AssistantEnvironmentRepository assistantEnvironmentRepository;
    private final MedicinePackageUnitRepository medicinePackageUnitRepository;
    private final ZoneRepository zoneRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Finding> evaluate(StockInboundCompletedEvent event, Long organizationId) {
        List<EnvRange> medicineRanges = assistantEnvironmentRepository
                .findMedicineRanges(organizationId, event.medicinePackageUnitId());

        if (medicineRanges.isEmpty()) {
            return Optional.empty();
        }

        Zone zone = zoneRepository.findById(event.zoneId()).orElse(null);

        if (zone == null) {
            return Optional.empty();
        }

        Map<String, EnvRange> zoneRanges = assistantEnvironmentRepository.findZoneRanges(event.zoneId())
                .stream()
                .collect(Collectors.toMap(EnvRange::environmentType, Function.identity(), (a, b) -> a));

        List<String> breaches = breaches(medicineRanges, zoneRanges);

        if (breaches.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Finding(
                FindingType.STORAGE_CONDITION,
                Severity.CRITICAL,
                medicineName(event.medicinePackageUnitId()),
                "%s 입고 · %s".formatted(zone.getName(), String.join(", ", breaches)),
                "이 구역의 환경 기준이 의약품 보관 기준을 벗어날 수 있습니다. 보관 위치를 확인하십시오.",
                new TargetReference(TargetType.ZONE, zone.getStorage().getId(), zone.getId())));
    }

    // 구역이 의약품 기준보다 더 넓게 허용할 때만 경고함 (구역이 더 좁은 것은 낭비일 뿐 사고가 아님)
    private List<String> breaches(List<EnvRange> medicineRanges, Map<String, EnvRange> zoneRanges) {
        List<String> breaches = new ArrayList<>();

        for (EnvRange medicine : medicineRanges) {
            EnvRange zone = zoneRanges.get(medicine.environmentType());

            if (zone == null) {
                continue;
            }

            String label = TYPE_LABEL.getOrDefault(medicine.environmentType(), medicine.environmentType());

            // null 은 제한 없음이 아니라 미설정이므로 판단하지 않음
            if (exceeds(zone.max(), medicine.max())) {
                breaches.add("%s 상한 %s > 기준 %s".formatted(label, plain(zone.max()), plain(medicine.max())));
            }

            if (fallsBelow(zone.min(), medicine.min())) {
                breaches.add("%s 하한 %s < 기준 %s".formatted(label, plain(zone.min()), plain(medicine.min())));
            }
        }

        return breaches;
    }

    private boolean exceeds(BigDecimal zoneMax, BigDecimal medicineMax) {
        return zoneMax != null && medicineMax != null && zoneMax.compareTo(medicineMax) > 0;
    }

    private boolean fallsBelow(BigDecimal zoneMin, BigDecimal medicineMin) {
        return zoneMin != null && medicineMin != null && zoneMin.compareTo(medicineMin) < 0;
    }

    // 8.00 대신 8 로 표시
    private String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private String medicineName(Long medicinePackageUnitId) {
        return medicinePackageUnitRepository.findById(medicinePackageUnitId)
                .map(unit -> "%s / %s".formatted(unit.getMedicine().getProductName(), unit.getPackUnit()))
                .orElse("해당 의약품");
    }
}
