package com.nhnacademy.inventory.inventories.inventory.operation.inbound.domain;

import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;

import com.nhnacademy.inventory.inventories.inventory.exception.ZoneNotAvailableException;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.exception.ExpirationdateMismatchException;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.exception.TransactionTypeInvalidException;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.inventories.transaction.dto.StockTransactionCommand;
import com.nhnacademy.inventory.inventories.transaction.service.StockTransactionService;

import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentRequest;

import com.nhnacademy.inventory.medicines.enviroment.service.MedicineEnvironmentService;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.exception.StorageInactiveException;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;


import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;




@Component
@RequiredArgsConstructor
@Slf4j
public class InboundOperation {

    private final MedicineInventoryRepository medicineInventoryRepository;
    private final StockTransactionService stockTransactionService;
    private final StorageRepository storageRepository;



    // 인벤토리에 저장
    public void inboundInventory(MedicinePackageUnit medicinePackageUnit, Zone zone, MedicineInventory inventory, MedicineInboundRequest request){


        log.info("==== 입고 등록 시작====");

        Storage storage = zone.getStorage();

        log.info("storage : {}", storage);

        //storage, zone active 검증



        if(zone.isActive()){
            throw new ZoneNotAvailableException();
        }



        if(request.transactionType() != TransactionType.INBOUND &&
                request.transactionType() != TransactionType.TRANSFER_IN &&
                request.transactionType() != TransactionType.INFO_CORRECTION_IN){

            throw new TransactionTypeInvalidException();
        }



        if(inventory == null){

            MedicineInventory medicineInventory = MedicineInventory.create(
                    medicinePackageUnit,
                    zone,
                    request.lotNumber().trim(),
                    request.expirationDate(),
                    request.quantity());

            medicineInventoryRepository.save(medicineInventory);

        }
        else{

            // 같은 제조번호 유통기한 다를 경우 에러 처리
            if(!inventory.getExpirationDate().equals(request.expirationDate())){
                throw new ExpirationdateMismatchException();
            }

            inventory.increaseQuantity(request.quantity());

        }



        stockTransactionService.createStockTransaction(new StockTransactionCommand(
                medicinePackageUnit,
                zone,
                request.transactionType(),
                request.quantity(),
                null,
                request.memo(),
                UserContext.getUserUuid()

        ));


        log.info("=== 입고 완료 ===");

    }






}
