package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.domain.QMedicineInventory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MedicineInventoryRepositoryImpl implements MedicineInventoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private static final QMedicineInventory inventory = QMedicineInventory.medicineInventory;


    @Override
    public Optional<MedicineInventory> findByMedicinePackageUnitIdAndZoneIdAndLotNumberAndExpirationDate
            (Long medicinePackageUnitId, Long zoneId, String lotNumber, LocalDate expiration) {

        MedicineInventory content = queryFactory.selectFrom(inventory)
                .where(inventory.medicinePackageUnit.id.eq(medicinePackageUnitId),
                        inventory.zone.id.eq(zoneId),
                        inventory.lotNumber.eq(lotNumber),
                        inventory.expirationDate.eq(expiration)

                        )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();


        return Optional.ofNullable(content);
    }
}
