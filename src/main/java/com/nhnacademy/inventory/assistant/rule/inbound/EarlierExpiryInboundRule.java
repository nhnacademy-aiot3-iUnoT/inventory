package com.nhnacademy.inventory.assistant.rule.inbound;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.FindingType;
import com.nhnacademy.inventory.assistant.rule.TargetReference;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import com.nhnacademy.inventory.assistant.dto.StockLot;
import com.nhnacademy.inventory.assistant.event.StockInboundCompletedEvent;
import com.nhnacademy.inventory.assistant.repository.AssistantStockRepository;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*
    방금 입고한 것보다 먼저 만료되는 같은 의약품이 같은 구역에 남아 있는지 확인
 */
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

        Zone zone = zoneRepository.findById(event.zoneId()).orElse(null);

        if (zone == null) {
            return Optional.empty();
        }

        int total = lots.stream().mapToInt(StockLot::quantity).sum();
        StockLot earliest = lots.getFirst();

        boolean alreadyExpired = earliest.expirationDate().isBefore(LocalDate.now());

        return Optional.of(new Finding(
                FindingType.EXPIRY_ORDER,
                alreadyExpired ? Severity.CRITICAL : Severity.INFO,
                medicineName(event.medicinePackageUnitId()),
                "%s · %s · %d개".formatted(zone.getName(), lotLabel(lots), total),
                explanation(earliest, alreadyExpired),
                new TargetReference(TargetType.PACK_UNIT, zone.getStorage().getId(), event.medicinePackageUnitId())));
    }

    private String explanation(StockLot earliest, boolean alreadyExpired) {
        String expiration = earliest.expirationDate().format(DATE_FORMAT);

        if (alreadyExpired) {
            return "%s에 만료된 재고가 남은 채로 입고되었습니다.".formatted(expiration);
        }

        return "%s 만료분이 남은 채로 입고되었습니다.".formatted(expiration);
    }

    private String medicineName(Long medicinePackageUnitId) {
        return medicinePackageUnitRepository.findById(medicinePackageUnitId)
                .map(unit -> "%s / %s".formatted(unit.getMedicine().getProductName(), unit.getPackUnit()))
                .orElse("해당 의약품");
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
