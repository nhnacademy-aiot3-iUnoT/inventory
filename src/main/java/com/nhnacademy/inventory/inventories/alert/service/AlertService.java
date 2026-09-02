package com.nhnacademy.inventory.inventories.alert.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.alert.domain.Alert;
import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import com.nhnacademy.inventory.inventories.alert.dto.AlertDeleteRequest;
import com.nhnacademy.inventory.inventories.alert.dto.AlertInfoResponse;
import com.nhnacademy.inventory.inventories.alert.dto.AlertCheckRequest;
import com.nhnacademy.inventory.inventories.alert.dto.AlertSearchCondition;
import com.nhnacademy.inventory.inventories.alert.exception.AlertNotFoundException;
import com.nhnacademy.inventory.inventories.alert.repository.AlertRepository;
import com.nhnacademy.inventory.inventories.inventory.service.InventoryService;
import com.nhnacademy.inventory.inventories.threshold.domain.StockThreshold;
import com.nhnacademy.inventory.inventories.threshold.repository.StockThresholdRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
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
public class AlertService {
    private final AlertRepository alertRepository;
    private final OrganizationMemberRepository memberRepository;
    private final ZoneRepository zoneRepository;
    private final MedicinePackageUnitRepository medicinePackageUnitRepository;
    private final StockThresholdRepository stockThresholdRepository;
    private final InventoryService inventoryService;

    @Transactional
    public void createLowStockAlert(Long zoneId, Long medicinePackageUnitId){
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구역"));
        MedicinePackageUnit medicinePackageUnit = medicinePackageUnitRepository.findById(medicinePackageUnitId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단위의약품"));

        Storage storage = zone.getStorage();

        StockThreshold stockThreshold = stockThresholdRepository
                .findByStorageAndMedicinePackageUnit(storage, medicinePackageUnit)
                .orElse(null);

        if(stockThreshold == null || !stockThreshold.getIsActive()){
            return;
        }

        long totalQuantity = inventoryService.getTotalQuantity(storage.getId(), medicinePackageUnit.getId());

        if(totalQuantity >= stockThreshold.getThreshold()){
            return;
        }

        List<OrganizationMember> memberList = memberRepository.findMemberByStorageId(storage.getId());

        for(OrganizationMember member : memberList){
            createAlert(
                    member,
                    AlertType.LOW_STOCK,
                    String.format("저장소: %s 의약품: %s 단위: %s 의 재고가 부족합니다. (현재 %d개)",
                            storage.getName(),
                            medicinePackageUnit.getMedicine().getProductName(),
                            medicinePackageUnit.getPackUnit(),
                            totalQuantity)
            );
        }
    }

    public Page<AlertInfoResponse> getAlerts(AlertSearchCondition condition, Pageable pageable){
        OrganizationMember member = validateOrganizationMember();

        return alertRepository.searchByCondition(member, condition, pageable);
    }

    public long getUncheckedAlertCount(){
        OrganizationMember member = validateOrganizationMember();

        return alertRepository.countByOrganizationMemberAndIsChecked(member, false);
    }

    @Transactional
    public void markAsChecked(AlertCheckRequest request){
        OrganizationMember member = validateOrganizationMember();

        List<Long> alertIds = request.alertIds();

        List<Alert> alerts = alertRepository.findAllByIdInAndOrganizationMember(alertIds, member);

        if(alertIds.size() != alerts.size()){
            throw new AlertNotFoundException();
        }

        alerts.forEach(Alert::markAsChecked);
    }

    @Transactional
    public void deleteAlerts(AlertDeleteRequest request){
        OrganizationMember member = validateOrganizationMember();

        List<Long> alertIds = request.alertIds();

        List<Alert> alerts = alertRepository.findAllByIdInAndOrganizationMember(alertIds, member);

        if(alertIds.size() != alerts.size()){
            throw new AlertNotFoundException();
        }

        alertRepository.deleteAll(alerts);
    }

    @Transactional
    public void deleteAllAlerts(){
        OrganizationMember member = validateOrganizationMember();

        List<Alert> alerts = alertRepository.findAllByOrganizationMember(member);

        alertRepository.deleteAll(alerts);
    }

    public void createAlert(OrganizationMember organizationMember, AlertType alertType, String message){
        Alert alert = Alert.builder()
                .organizationMember(organizationMember)
                .alertType(alertType)
                .message(message)
                .isChecked(false)
                .build();

        alertRepository.save(alert);
    }

    private OrganizationMember validateOrganizationMember(){
        return memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);
    }
}
