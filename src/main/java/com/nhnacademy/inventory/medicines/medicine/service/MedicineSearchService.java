package com.nhnacademy.inventory.medicines.medicine.service;


import com.nhnacademy.inventory.medicines.medicine.domain.SearchType;


import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageDetailResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageSearchResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicineSearchRequest;
import com.nhnacademy.inventory.medicines.medicine.exception.*;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineSearchService {

    private final MedicinePackageUnitRepository packageUnitRepository;


    // 의약품 정보 검색
    @Transactional(readOnly = true)
    public Page<MedicinePackageSearchResponse> getMedicines(MedicineSearchRequest request, Pageable pageable){

        log.info("==== 의약품 정보 검색 시작 ====");

        String trimmed = request.search().trim();

        Page<MedicinePackageSearchResponse> searchResponse;

        if(request.searchType() == SearchType.PRODUCT_NAME){

            searchResponse = packageUnitRepository.findAllWithMedicineByProductName(trimmed,pageable);
            log.info("product name : {} , searchResponse : {}",trimmed,searchResponse.getContent());

        }

        else{

            if(!trimmed.matches("\\d{9}")){
                throw new ItemCodeInvalidException();
            }

            // 빈 리스트 반환
            searchResponse = packageUnitRepository.findAllWithMedicineByItemCode(trimmed,pageable);
            log.info("item code : {} , searchResponse : {}",request.search(),searchResponse.getContent());

        }


        log.info("searchType : {}, search: {}, totalElements: {}",request.searchType(),trimmed,searchResponse.getTotalElements());

        return searchResponse;


    }


    // 특정 의약품 조회
    @Transactional(readOnly = true)
    public MedicinePackageDetailResponse getDetail(Long medicinePackageUnitId){

        MedicinePackageDetailResponse detailResponse = packageUnitRepository.
                findDetailMedicine(medicinePackageUnitId)
                .orElseThrow(PackUnitNotFoundException::new);

        log.info("detail response: {}", detailResponse);

        return detailResponse;

    }







}
