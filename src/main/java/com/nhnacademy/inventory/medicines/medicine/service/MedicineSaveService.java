package com.nhnacademy.inventory.medicines.medicine.service;

import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicineResponse;
import com.nhnacademy.inventory.medicines.medicine.exception.MedicineNotFoundException;
import com.nhnacademy.inventory.medicines.medicine.mapper.MedicineMapper;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicineRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineSaveService {

    private final MedicineRepository medicineRepository;
    private final MedicinePackageUnitRepository packageUnitRepository;
    private final MedicineMapper medicineMapper;


    // 데이터 저장
    @Transactional
    public void saveMedicines(List<MedicineResponse> responses){

        int savedCount = 0;
        int failedCount = 0;


        log.info("====== 의약품 저장 시작 =========");
        for(MedicineResponse medicineResponse : responses){

            String itemCode = medicineResponse.itemCode();


            if(itemCode == null || itemCode.isBlank()){
                failedCount ++;
                continue;
            }

            if(medicineRepository.existsByItemCode(medicineResponse.itemCode())){
                failedCount ++;
                continue;
            }


            Medicine medicine = medicineMapper.toMedicineEntity(medicineResponse);
            Medicine savedMedicine = medicineRepository.save(medicine);

            List<MedicinePackageUnit> savedPackageUnits= medicineMapper.toPackageUnitEntities(savedMedicine,medicineResponse);
            packageUnitRepository.saveAll(savedPackageUnits);

            savedCount++;

        }




        log.info("의약품 페이지 저장 완료 요청= {}, 저장= {}, 건너뜀= {}",responses.size(),savedCount,failedCount);



    }






}
