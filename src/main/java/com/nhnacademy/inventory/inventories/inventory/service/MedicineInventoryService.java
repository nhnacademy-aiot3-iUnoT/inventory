package com.nhnacademy.inventory.inventories.inventory.service;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.inventories.inventory.exception.ZoneNotAvailableException;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionCommand;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;
import com.nhnacademy.inventory.medicines.enviroment.service.MedicineEnvironmentService;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.exception.PackUnitNotFoundException;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import com.nhnacademy.inventory.organizations.zone.exception.ZoneNotFoundException;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineInventoryService {

    private final MedicineInventoryRepository medicineInventoryRepository;
    private final MedicinePackageUnitRepository medicinePackageUnitRepository;
    private final ZoneRepository zoneRepository;
    private final StockTransactionService stockTransactionService;
    private final MedicineEnvironmentService medicineEnvironmentService;


    //입고
    @Transactional
    public void inbound(MedicineInboundRequest request){


        log.info("입고 요청: medicinePackageUnitId = {}, zoneId = {}, lotNumber = {}, quantity = {}",
                request.medicinePackageUnitId(),
                request.zoneId(),
                request.lotNumber(),
                request.quantity());

        String lotNumber = request.lotNumber().trim();

        MedicinePackageUnit medicinePackageUnit = medicinePackageUnitRepository.findById(request.medicinePackageUnitId())
                .orElseThrow(PackUnitNotFoundException::new);

        Zone zone = zoneRepository.findById(request.zoneId())
                .orElseThrow(ZoneNotFoundException::new);

        validationAvailableZone(zone);


        MedicineInventory existInventory =  medicineInventoryRepository.findByMedicinePackageUnitIdAndZoneIdAndLotNumberAndExpirationDate(
                request.medicinePackageUnitId(),
                request.zoneId(),
                lotNumber,
                request.expirationDate()
        ).orElse(null);

//        if(existInventory.isPresent()){
//            existInventory.get().increaseQuantity(request.quantity());
//
//        }
//        else{
//
//            MedicineInventory newInventory = MedicineInventory.create(
//                    medicinePackageUnit,
//                    zone,
//                    lotNumber,
//                    request.expirationDate(),
//                    request.quantity()
//            );
//            medicineInventoryRepository.save(newInventory);
//
//        }

        // 재고 변동 내역 등록 - 입고
        StockTransactionCommand command = new StockTransactionCommand(
                medicinePackageUnit,
                zone,
                TransactionType.INBOUND,
                request.quantity(),
                null,
                null,
                UserContext.getUserUuid()
        );

        stockTransactionService.createStockTransaction(command);

    }

    private void validationAvailableZone(Zone zone){

        if(zone.getStatus() != ZoneStatus.ACTIVE){
            throw new ZoneNotAvailableException();
        }

    }



}
