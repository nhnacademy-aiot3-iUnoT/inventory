package com.nhnacademy.inventory.medicines.medicine.repository;

import com.nhnacademy.inventory.global.config.QuerydslConfig;

import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageDetailResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageSearchResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@Import(QuerydslConfig.class)
@Sql("/sql/medicine-test-data.sql")
class MedicinePackageUnitRepositoryImplTest {


    @Autowired
    MedicinePackageUnitRepository medicinePackageUnitRepository;


    @Test
    @DisplayName("제품명으로 의약품 포장단위를 조회")
    void findAllWithMedicineByProductName() {

        Pageable pageable = PageRequest.of(0,10);

        Page<MedicinePackageSearchResponse> result =
                medicinePackageUnitRepository.findAllWithMedicineByProductName("타이레놀",pageable);


        assertEquals(3,result.getContent().size());
        assertEquals(3,result.getTotalElements());

        List<MedicinePackageSearchResponse> response =
                result.getContent();


        assertAll(

                () -> assertEquals("타이레놀정",response.getFirst().productName()),
                () -> assertEquals("001",response.getFirst().itemCode()),
                () -> assertEquals("A제약",response.getFirst().companyName()),
                () -> assertEquals(1L,response.getFirst().medicineId()),
                () -> assertEquals("10정",response.getFirst().packUnit()),


                () -> assertEquals("타이레놀8시간이알서방정",response.get(2).productName()),
                () -> assertEquals("002",response.get(2).itemCode()),
                () -> assertEquals("B제약",response.get(2).companyName()),
                () -> assertEquals(2L,response.get(2).medicineId()),
                () -> assertEquals("20정",response.get(2).packUnit())


        );

    }

    @Test
    @DisplayName("품목기준코드로 조회")
    void findAllWithMedicineByItemCode() {


        Pageable pageable = Pageable.ofSize(10);

        Page<MedicinePackageSearchResponse> page = medicinePackageUnitRepository.findAllWithMedicineByItemCode("001",pageable);


        List<MedicinePackageSearchResponse> result = page.getContent();


        assertAll(

                () -> assertEquals(2,page.getTotalElements()),
                () -> assertEquals(2,page.getContent().size()),

                () -> assertEquals(1,result.getFirst().medicineId()),
                () -> assertEquals("001",result.getFirst().itemCode()),
                () -> assertEquals("타이레놀정",result.getFirst().productName()),
                () -> assertEquals("A제약",result.getFirst().companyName()),
                () -> assertEquals("10정",result.getFirst().packUnit()),

                () -> assertEquals("30정",result.get(1).packUnit())


        );



    }

    @Test
    @DisplayName("의약품 상세 조회 테스트")
    void findDetailMedicine() {


        MedicinePackageDetailResponse detailResponse = medicinePackageUnitRepository.findDetailMedicine(3L)
                        .orElse(null);

        assertNotNull(detailResponse);

        assertAll(

                () -> assertEquals(2L,detailResponse.medicineId()),
                () -> assertEquals(3L,detailResponse.packageUnitId()),
                () -> assertEquals("002",detailResponse.itemCode()),
                () -> assertEquals("타이레놀8시간이알서방정",detailResponse.productName()),
                () -> assertEquals("B제약",detailResponse.companyName()),
                () -> assertEquals("실온보관",detailResponse.storageMethod()),
                () -> assertEquals("12개월",detailResponse.validityPeriod()),
                () -> assertEquals("20정",detailResponse.packUnit()),
                () -> assertNull(detailResponse.narcoticKindCode())

        );


    }



    @Test
    @DisplayName("의약품 조회 없음")
    void notFoundMedicinesTest(){


        // 의약품 제품명 조회 없음
        Pageable pageable = Pageable.ofSize(10);
        Page<MedicinePackageSearchResponse> result1 = medicinePackageUnitRepository.findAllWithMedicineByProductName("테스트 중",pageable);


        assertEquals(0,result1.getTotalElements());
        assertEquals(0,result1.getContent().size());


        // 의약품 품목기준코드 조회 없음

        Page<MedicinePackageSearchResponse> result2 = medicinePackageUnitRepository.findAllWithMedicineByItemCode("005",pageable);

        assertEquals(0,result2.getTotalElements());
        assertEquals(0,result2.getContent().size());



    }



    @Test
    @DisplayName("특정 의약품 조회 없음")
    void notFoundDetailMedicineTest(){


        MedicinePackageDetailResponse detailResponse = medicinePackageUnitRepository.findDetailMedicine(5L)
                        .orElse(null);


        assertNull(detailResponse);






    }







}