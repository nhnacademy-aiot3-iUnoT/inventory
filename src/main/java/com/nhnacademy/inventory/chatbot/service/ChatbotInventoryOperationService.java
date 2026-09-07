package com.nhnacademy.inventory.chatbot.service;

import com.nhnacademy.inventory.chatbot.dto.MedicinePackageUnitTargetRow;
import com.nhnacademy.inventory.chatbot.dto.ZoneTargetRow;
import com.nhnacademy.inventory.chatbot.dto.query.FindMedicinePackageUnitTargetQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindZoneTargetQuery;
import com.nhnacademy.inventory.chatbot.dto.request.InboundToolRequest;
import com.nhnacademy.inventory.chatbot.dto.request.OutboundToolRequest;
import com.nhnacademy.inventory.chatbot.dto.response.InboundToolResponse;
import com.nhnacademy.inventory.chatbot.dto.response.OutboundToolResponse;
import com.nhnacademy.inventory.chatbot.repository.MedicineInventoryChatbotRepository;
import com.nhnacademy.inventory.global.error.BaseException;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.service.InboundService;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.service.MedicineOutboundService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotInventoryOperationService {
    private static final int MAX_TARGET_RESULTS = 5;

    private final MedicineInventoryChatbotRepository inventoryRepository;
    private final ChatbotStorageAccessService accessService;
    private final InboundService inboundService;
    private final MedicineOutboundService outboundService;
    private final Validator validator;

    public InboundToolResponse inbound(InboundToolRequest request) {
        Optional<String> validationError = validate(request);
        if (validationError.isPresent()) {
            return InboundToolResponse.failure(validationError.get());
        }

        try {
            OperationTarget target = resolveTarget(
                    request.medicinePackageUnitId(),
                    request.medicineName(),
                    request.packUnit(),
                    request.zoneId(),
                    request.storageName(),
                    request.zoneName(),
                    true
            );
            inboundService.createInbound(new MedicineInboundRequest(
                    target.packageUnit().packageUnitId(),
                    target.zone().zoneId(),
                    request.lotNumber().trim(),
                    request.expirationDate(),
                    request.quantity(),
                    trimToNull(request.memo()),
                    null
            ));

            return InboundToolResponse.success(
                    target.packageUnit().medicineName(),
                    target.packageUnit().packUnit(),
                    target.zone().storageName(),
                    target.zone().zoneName(),
                    request.lotNumber().trim(),
                    request.expirationDate(),
                    request.quantity()
            );
        } catch (TargetResolutionException | BaseException e) {
            return InboundToolResponse.failure(e.getMessage());
        }
    }

    public OutboundToolResponse outbound(OutboundToolRequest request) {
        Optional<String> validationError = validate(request);
        if (validationError.isPresent()) {
            return OutboundToolResponse.failure(validationError.get());
        }

        try {
            var target = outboundService.getOutboundTarget(
                    request.inventoryId()
            );

            outboundService.outbound(
                    request.inventoryId(),
                    new MedicineOutboundRequest(
                            target.medicinePackageUnitId(),
                            request.quantity(),
                            target.zoneId(),
                            request.reason(),
                            trimToNull(request.memo())
                    )
            );

            return OutboundToolResponse.success(
                    target.productName(),
                    target.packUnit(),
                    target.storageName(),
                    target.zoneName(),
                    request.quantity(),
                    request.reason()
            );
        } catch (BaseException e) {
            return OutboundToolResponse.failure(e.getMessage());
        }
    }

    private OperationTarget resolveTarget(
            Long medicinePackageUnitId,
            String medicineName,
            String packUnit,
            Long zoneId,
            String storageName,
            String zoneName,
            boolean supportsIdSelection
    ) {
        MedicinePackageUnitTargetRow packageUnit = resolvePackageUnitTarget(
                medicinePackageUnitId,
                medicineName,
                packUnit,
                supportsIdSelection
        );
        List<Long> storageIds = accessService.getAccessibleStorageIds();
        ZoneTargetRow zone = resolveZoneTarget(
                zoneId,
                storageIds,
                storageName,
                zoneName,
                supportsIdSelection
        );

        return new OperationTarget(packageUnit, zone);
    }

    private MedicinePackageUnitTargetRow resolvePackageUnitTarget(
            Long medicinePackageUnitId,
            String medicineName,
            String packUnit,
            boolean supportsIdSelection
    ) {
        if (medicinePackageUnitId != null) {
            return inventoryRepository.findPackageUnitTargetById(medicinePackageUnitId)
                    .orElseThrow(() -> new TargetResolutionException(
                            "선택한 의약품 포장단위를 찾을 수 없습니다."
                    ));
        }

        List<MedicinePackageUnitTargetRow> packageUnits = inventoryRepository.findPackageUnitTargets(
                new FindMedicinePackageUnitTargetQuery(
                        medicineName.trim(),
                        packUnit.trim(),
                        MAX_TARGET_RESULTS
                )
        );

        if (packageUnits.isEmpty()) {
            throw new TargetResolutionException("입력한 의약품명과 포장단위에 일치하는 의약품을 찾을 수 없습니다.");
        }

        if (packageUnits.size() > 1) {
            String candidates = packageUnits.stream()
                    .map(row -> formatPackageUnitCandidate(row, supportsIdSelection))
                    .collect(Collectors.joining(", "));
            String nextAction = supportsIdSelection
                    ? ". 사용자가 선택한 후보의 medicinePackageUnitId를 다음 입고 요청에 포함해주세요."
                    : ". 의약품명과 포장단위를 더 정확하게 입력해주세요.";
            throw new TargetResolutionException(
                    "여러 의약품 포장단위가 검색되었습니다: " + candidates + nextAction
            );
        }

        return packageUnits.getFirst();
    }

    private ZoneTargetRow resolveZoneTarget(
            Long zoneId,
            List<Long> storageIds,
            String storageName,
            String zoneName,
            boolean supportsIdSelection
    ) {
        if (zoneId != null) {
            return inventoryRepository.findZoneTargetById(zoneId, storageIds)
                    .orElseThrow(() -> new TargetResolutionException(
                            "선택한 구역을 찾을 수 없거나 접근할 수 없습니다."
                    ));
        }

        List<ZoneTargetRow> zones = inventoryRepository.findZoneTargets(
                new FindZoneTargetQuery(
                        storageIds,
                        storageName.trim(),
                        zoneName.trim(),
                        MAX_TARGET_RESULTS
                )
        );

        if (zones.isEmpty()) {
            throw new TargetResolutionException("접근 가능한 저장소에서 입력한 구역을 찾을 수 없습니다.");
        }

        if (zones.size() > 1) {
            String candidates = zones.stream()
                    .map(row -> formatZoneCandidate(row, supportsIdSelection))
                    .collect(Collectors.joining(", "));
            String nextAction = supportsIdSelection
                    ? ". 사용자가 선택한 후보의 zoneId를 다음 입고 요청에 포함해주세요."
                    : ". 저장소명과 구역명을 더 정확하게 입력해주세요.";
            throw new TargetResolutionException(
                    "여러 저장소 구역이 검색되었습니다: " + candidates + nextAction
            );
        }

        return zones.getFirst();
    }

    private String formatPackageUnitCandidate(
            MedicinePackageUnitTargetRow row,
            boolean includeId
    ) {
        if (includeId) {
            return "[medicinePackageUnitId=%d] %s (%s)".formatted(
                    row.packageUnitId(),
                    row.medicineName(),
                    row.packUnit()
            );
        }
        return "%s (%s)".formatted(row.medicineName(), row.packUnit());
    }

    private String formatZoneCandidate(ZoneTargetRow row, boolean includeId) {
        if (includeId) {
            return "[zoneId=%d] %s / %s".formatted(
                    row.zoneId(),
                    row.storageName(),
                    row.zoneName()
            );
        }
        return "%s / %s".formatted(row.storageName(), row.zoneName());
    }

    private <T> Optional<String> validate(T request) {
        if (request == null) {
            return Optional.of("입출고 요청 정보가 필요합니다.");
        }

        return validator.validate(request).stream()
                .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
                .map(ConstraintViolation::getMessage)
                .findFirst();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record OperationTarget(
            MedicinePackageUnitTargetRow packageUnit,
            ZoneTargetRow zone
    ) {
    }

    private static class TargetResolutionException extends RuntimeException {
        private TargetResolutionException(String message) {
            super(message);
        }
    }
}
