package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


import java.time.LocalDate;


public interface MedicineInventoryRepository extends JpaRepository<MedicineInventory, Long>, MedicineInventoryRepositoryCustom{


    @Query("""
            
            select coalesce(sum(mi.currentQuantity),0)
            from MedicineInventory mi
            where mi.zone in (:zones)
                        and mi.medicinePackageUnit = :packUnitId
            group by mi.zone,mi.medicinePackageUnit
            """)
    Long findByZoneAndPackUnitSum(@Param("zones")List<Zone> zones,@Param("packUnitId") Long packUnitId);



    long countByZone_StorageAndExpirationDateBefore(Storage zoneStorage, LocalDate expirationDateBefore);

    long countByZone_StorageAndExpirationDateBetween(Storage zoneStorage, LocalDate expirationDateAfter, LocalDate expirationDateBefore);
}
