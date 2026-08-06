package com.nhnacademy.inventory.inventories.alert.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.alert.domain.Alert;
import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import com.nhnacademy.inventory.inventories.alert.dto.AlertDeleteRequest;
import com.nhnacademy.inventory.inventories.alert.dto.AlertInfoResponse;
import com.nhnacademy.inventory.inventories.alert.dto.AlertReadRequest;
import com.nhnacademy.inventory.inventories.alert.dto.AlertSearchCondition;
import com.nhnacademy.inventory.inventories.alert.exception.AlertNotFoundException;
import com.nhnacademy.inventory.inventories.alert.repository.AlertRepository;
import com.nhnacademy.inventory.inventories.threshold.domain.StockThreshold;
import com.nhnacademy.inventory.inventories.threshold.repository.StockThresholdRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
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
import java.util.Objects;

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
    private final OrganizationRepository organizationRepository;

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

        int totalQuantity = 30; // 인벤토리 레포 또는 서비스에서 개수조회 메서드 완성되면 교체하기

        if(totalQuantity >= stockThreshold.getThreshold()){
            return;
        }

        createAlert(
                storage.getOrganization(),
                AlertType.LOW_STOCK,
                String.format("저장소: %s 의약품: %s 단위: %s 의 재고가 부족합니다. (현재 %d개)",
                        storage.getName(),
                        medicinePackageUnit.getMedicine().getProductName(),
                        medicinePackageUnit.getPackUnit(),
                        totalQuantity)
        );
    }

    public Page<AlertInfoResponse> getAlerts(Long organizationId, AlertSearchCondition condition, Pageable pageable){
        Organization organization = validateOrganizationMember(organizationId);

        return alertRepository.searchByCondition(organization, condition, pageable);
    }

    public long getUnreadAlertCount(Long organizationId){
        Organization organization = validateOrganizationMember(organizationId);

        return alertRepository.countByOrganizationAndIsRead(organization, false);
    }

    @Transactional
    public void markAsRead(Long organizationId, AlertReadRequest request){
        Organization organization = validateOrganizationMember(organizationId);

        List<Long> alertIds = request.alertIds();

        List<Alert> alerts = alertRepository.findAllByIdInAndOrganization(alertIds, organization);

        if(alertIds.size() != alerts.size()){
            throw new AlertNotFoundException();
        }

        alerts.forEach(Alert::markAsRead);
    }

    @Transactional
    public void deleteAlerts(Long organizationId, AlertDeleteRequest request){
        Organization organization = validateOrganizationMember(organizationId);

        List<Long> alertIds = request.alertIds();

        List<Alert> alerts = alertRepository.findAllByIdInAndOrganization(alertIds, organization);

        if(alertIds.size() != alerts.size()){
            throw new AlertNotFoundException();
        }

        alertRepository.deleteAll(alerts);
    }

    @Transactional
    public void deleteAllAlerts(Long organizationId){
        Organization organization = validateOrganizationMember(organizationId);

        List<Alert> alerts = alertRepository.findAllByOrganization(organization);

        alertRepository.deleteAll(alerts);
    }

    @Transactional
    public void createAlert(Long organizationId, AlertType alertType, String message){
        Organization organization = organizationRepository.getReferenceById(organizationId);
        createAlert(organization, alertType, message);
    }

    private void createAlert(Organization organization, AlertType alertType, String message){
        Alert alert = Alert.builder()
                .organization(organization)
                .alertType(alertType)
                .message(message)
                .isRead(false)
                .build();

        alertRepository.save(alert);
    }

    private Organization validateOrganizationMember(Long organizationId){
        OrganizationMember member = memberRepository.findByAccountUuid(UserContext.getUserUuid())
                .orElseThrow(ForbiddenException::new);

        if(!Objects.equals(member.getOrganization().getId(), organizationId)){
            throw new ForbiddenException();
        }

        return member.getOrganization();
    }
}
