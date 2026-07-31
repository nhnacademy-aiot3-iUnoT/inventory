package com.nhnacademy.inventory.medicines.medicine.service;

import com.nhnacademy.inventory.global.error.GlobalErrorCode;
import com.nhnacademy.inventory.medicines.error.MedicineErrorCode;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageDetailResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageSearchResponse;
import com.nhnacademy.inventory.medicines.medicine.exception.PackUnitIdInvalidException;
import com.nhnacademy.inventory.medicines.medicine.exception.PackUnitNotFoundException;
import com.nhnacademy.inventory.medicines.medicine.exception.ProductNameRequiredException;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineSearchService {

    private final MedicinePackageUnitRepository packageUnitRepository;


    // 의약품 제품명 검색 (제품명 조회용)
    public Page<MedicinePackageSearchResponse> searchByProductName(String productName, Pageable pageable){

        if(productName == null || productName.isBlank()){
            throw new ProductNameRequiredException(MedicineErrorCode.PRODUCT_NAME_REQUIRED);
        }

        String trimmed = productName.trim();
        log.info("trimmed: {}",trimmed);
        log.info("pageable number: {} pageable size: {}",pageable.getPageNumber(),pageable.getPageSize());

        Page<MedicinePackageUnit> units = packageUnitRepository.findAllWithMedicineByProductName(trimmed,pageable);

        log.info("unit: {}",units);
        log.info("전체 데이터 수: {}",units.getTotalElements());
        log.info("전체 페이지 수: {}",units.getTotalPages());


//        Long medicineId,
//        Long packageUnitId,
//        String itemCode,
//        String productName,
//        String companyName,
//        String packUnit


        Page<MedicinePackageSearchResponse> searchResponses =  units.map(unit -> {

            Medicine medicine = unit.getMedicine();

            return new MedicinePackageSearchResponse(
                    medicine.getId(),
                    unit.getId(),
                    medicine.getItemCode(),
                    medicine.getProductName(),
                    medicine.getCompanyName(),
                    unit.getPackUnit());

        });

        log.info("searchResponse list: {}",searchResponses.getContent());

        return searchResponses;
    }

    //의약품 상세 조회
    public MedicinePackageDetailResponse searchDetailMedicine(Long packageUnitId){

            if(packageUnitId == null || packageUnitId <= 0L){
                throw new PackUnitIdInvalidException(MedicineErrorCode.PACK_UNIT_INVALID);
            }

            log.info("packUnitId: {}",packageUnitId);

            Optional<MedicinePackageUnit> optPackageUnit = packageUnitRepository.findWithMedicineByPackUnitId(packageUnitId);

            MedicinePackageUnit packageUnit = optPackageUnit.orElseThrow(() -> new PackUnitNotFoundException(MedicineErrorCode.PACK_UNIT_NOT_FOUND));
            log.info("packageUnit: {}",packageUnit);


            return toDetailResponse(packageUnit.getMedicine(),packageUnit);

    }


    private MedicinePackageDetailResponse toDetailResponse(Medicine medicine, MedicinePackageUnit packageUnit){


        MedicinePackageDetailResponse response =  new MedicinePackageDetailResponse(
                medicine.getId(),
                packageUnit.getId(),
                medicine.getItemCode(),
                medicine.getProductName(),
                medicine.getCompanyName(),
                medicine.getStorageMethod(),
                medicine.getValidityPeriod(),
                medicine.getNarcoticKindCode()

        );
        log.info("response: {}",response);

        return response;

    }





}
