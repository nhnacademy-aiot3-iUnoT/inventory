package com.nhnacademy.inventory.inventories.expiration.service;

import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import com.nhnacademy.inventory.inventories.alert.service.AlertService;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.domain.OrganizationStatus;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpirationAlertScheduler {

    private final MedicineInventoryRepository inventoryRepository;
    private final OrganizationRepository organizationRepository;
    private final StorageRepository storageRepository;
    private final AlertService alertService;

    @Scheduled(cron = "0 0 3 * * *")
    @SchedulerLock(
            name = "checkExpirationAndCreateAlert",
            lockAtLeastFor = "PT5M",
            lockAtMostFor = "PT15M"
    )
    @Transactional
    public void checkExpirationAndCreateAlert(){
        List<Organization> organizationList = organizationRepository.findAllByStatus(OrganizationStatus.ACTIVE);

        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(7);

        for(Organization organization : organizationList){
            List<Storage> storageList = storageRepository.findAllByOrganizationAndStatus(organization, StorageStatus.ACTIVE);

            for(Storage storage : storageList){
                long expiredCount = inventoryRepository.countByZone_StorageAndExpirationDateBefore(storage, today);
                long warningCount = inventoryRepository.countByZone_StorageAndExpirationDateBetween(storage, today, warningDate);

                if(expiredCount > 0 || warningCount > 0){
                    LocalDateTime now = LocalDateTime.now();
                    String formattedNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    StringBuilder sb = new StringBuilder();
                    sb.append("[").append(storage.getName()).append("] 유통기한 임박 알림(").append(formattedNow).append(" 기준)\n");

                    if (expiredCount > 0) {
                        sb.append("유통기한 경과 재고: ").append(expiredCount).append("건 (즉시 폐기/확인 필요)\n");
                    }
                    if (warningCount > 0) {
                        sb.append("유통기한 임박 재고(7일 이내): ").append(warningCount).append("건");
                    }

                    alertService.createAlert(organization.getId(), AlertType.EXPIRING, sb.toString());
                }
            }
        }
    }
}
