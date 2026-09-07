package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;


import java.time.LocalDate;


public interface MedicineInventoryRepository extends JpaRepository<MedicineInventory, Long>, MedicineInventoryRepositoryCustom {

    @Query("""
            select coalesce(sum(mi.currentQuantity), 0)
            from MedicineInventory mi
            where mi.medicinePackageUnit.id = :medicinePackageUnitId
              and mi.zone.id = :zoneId
              and mi.managementStatus = :managementStatus
              and mi.expirationDate >= CURRENT_DATE
              and mi.currentQuantity > 0
            """)
    long sumAvailableQuantity(
            @Param("medicinePackageUnitId") Long medicinePackageUnitId,
            @Param("zoneId") Long zoneId,
            @Param("managementStatus") ManagementStatus managementStatus
    );

    @Query("""
            
            select coalesce(sum(mi.currentQuantity),0)
            from MedicineInventory mi
            where mi.zone in (:zones)
                        and mi.medicinePackageUnit.id = :packUnitId
            group by mi.zone,mi.medicinePackageUnit
            """)
    Long findByZoneAndPackUnitSum(@Param("zones")List<Zone> zones,@Param("packUnitId") Long packUnitId);


    long countByZone_StorageAndExpirationDateBeforeAndManagementStatusIn(Storage zoneStorage, LocalDate expirationDateBefore, Collection<ManagementStatus> managementStatuses);

    long countByZone_StorageAndExpirationDateBetweenAndManagementStatusIn(Storage zoneStorage, LocalDate expirationDateAfter, LocalDate expirationDateBefore, Collection<ManagementStatus> managementStatuses);

    List<MedicineInventory> findAllByZoneAndManagementStatusIn(Zone zone, Collection<ManagementStatus> managementStatuses);
}
