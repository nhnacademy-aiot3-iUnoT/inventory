package com.nhnacademy.inventory.enviroments.event.service;

import com.nhnacademy.inventory.enviroments.event.domain.EnvironmentEvent;
import com.nhnacademy.inventory.enviroments.event.dto.*;
import com.nhnacademy.inventory.enviroments.event.repository.EnvironmentEventRepository;
import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import com.nhnacademy.inventory.inventories.alert.service.AlertService;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.service.ZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnvironmentEventService {
    private final EnvironmentEventRepository environmentEventRepository;
    private final MedicineInventoryRepository medicineInventoryRepository;
    private final ZoneService zoneService;
    private final AlertService alertService;

    @Transactional
    public void createEnvironmentEvent(EnvironmentEventCreateRequest request){
        Zone zone = zoneService.findZone(request.zoneId());

        EnvironmentEvent event = EnvironmentEvent.builder()
                .zone(zone)
                .detectedValue(request.detectedValue())
                .thresholdValue(request.thresholdValue())
                .environmentType(request.environmentType())
                .breachType(request.breachType())
                .build();

        environmentEventRepository.save(event);

        List<MedicineInventory> inventoryList = medicineInventoryRepository.findAllByZoneAndManagementStatus(zone, ManagementStatus.NORMAL);

        if (inventoryList != null) {
            for (MedicineInventory inventory : inventoryList) {
                inventory.setManagementStatus(ManagementStatus.UNDER_REVIEW);
            }

            String storageName = zone.getStorage().getName();
            String zoneName = zone.getName();
            int reviewCount = inventoryList.size();

            String message = String.format("[%s] %s - %s 환경 이상 발생! (폐기 검토 대상 재고: %d건)",
                    storageName, zoneName, request.environmentType(), reviewCount);

            alertService.createAlert(
                    zone.getStorage().getOrganization().getId(),
                    AlertType.ENV_WARNING,
                    message
            );
        }
    }

    public Page<EnvironmentEventInfoResponse> searchEnvironmentEvents(Long zoneId, EnvironmentEventSearchCondition condition, Pageable pageable){
        Zone targetZone = zoneService.findZone(zoneId);

        return environmentEventRepository.findEnvironmentEvents(targetZone, condition, pageable);
    }

    public List<EnvironmentEventItemResponse> getEnvironmentEvents(EnvironmentEventCheckRequest request){
        Zone targetZone = zoneService.findZone(request.zoneId());

        List<EnvironmentEvent> eventList = environmentEventRepository
                .findAllByZoneAndCreatedAtAfterOrderByCreatedAtDesc(targetZone, request.lastReviewAt());

        return eventList.stream()
                .map(EnvironmentEventItemResponse::from)
                .toList();
    }
}
