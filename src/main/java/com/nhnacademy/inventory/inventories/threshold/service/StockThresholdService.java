package com.nhnacademy.inventory.inventories.threshold.service;

import com.nhnacademy.inventory.inventories.threshold.domain.StockThreshold;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdSaveRequest;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdInfoResponse;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdUpdateRequest;
import com.nhnacademy.inventory.inventories.threshold.exception.StockThresholdNotFoundException;
import com.nhnacademy.inventory.inventories.threshold.repository.StockThresholdRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.exception.PackUnitNotFoundException;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockThresholdService {
    private final StockThresholdRepository stockThresholdRepository;
    private final MedicinePackageUnitRepository medicinePackageUnitRepository;
    private final StorageService storageService;

    @Transactional
    public StockThresholdInfoResponse saveStockThreshold(Long storageId, StockThresholdSaveRequest request){
        Storage storage = storageService.validateOwnerAndGetStorage(storageId);

        MedicinePackageUnit medicinePackageUnit = medicinePackageUnitRepository
                .findById(request.medicinePackageUnitId())
                .orElseThrow(PackUnitNotFoundException::new); // 나중에 medicinePackageUnitService만들어지면 메서드 가져오기

        StockThreshold stockThreshold = stockThresholdRepository
                .findByStorageAndMedicinePackageUnit(storage, medicinePackageUnit)
                .map(existing -> {
                    existing.updateThreshold(request.stockThreshold());
                    return existing;
                })
                .orElseGet(() -> stockThresholdRepository.save(
                        StockThreshold.builder()
                                .storage(storage)
                                .medicinePackageUnit(medicinePackageUnit)
                                .threshold(request.stockThreshold())
                                .isActive(true)
                                .build()
                ));

        return StockThresholdInfoResponse.from(stockThreshold);
    }

    public List<StockThresholdInfoResponse> getStockThresholds(Long storageId){
        Storage storage = storageService.validateMemberAndGetStorage(storageId);

        List<StockThreshold> stockThresholds = stockThresholdRepository.findAllByStorage(storage);

        return stockThresholds.stream()
                .map(StockThresholdInfoResponse::from)
                .toList();
    }

    @Transactional
    public StockThresholdInfoResponse updateStockThreshold(Long storageId, Long stockThresholdId, StockThresholdUpdateRequest request){
        StockThreshold stockThreshold = findByIdAndValidate(stockThresholdId, storageId);

        stockThreshold.updateThreshold(request.stockThreshold());
        stockThreshold.updateIsActive(request.isActive());

        return StockThresholdInfoResponse.from(stockThreshold);
    }

    @Transactional
    public void deleteStockThreshold(Long storageId, Long stockThresholdId){
        StockThreshold stockThreshold = findByIdAndValidate(stockThresholdId, storageId);

        stockThresholdRepository.delete(stockThreshold);
    }

    private StockThreshold findByIdAndValidate(Long stockThresholdId, Long storageId){
        Storage storage = storageService.validateOwnerAndGetStorage(storageId);

        return stockThresholdRepository.findByIdAndStorage(stockThresholdId, storage)
                .orElseThrow(StockThresholdNotFoundException::new);
    }
}
