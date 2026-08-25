package com.nhnacademy.inventory.medicines.medicine.service;

import com.nhnacademy.inventory.medicines.medicine.domain.SearchType;
import com.nhnacademy.inventory.medicines.medicine.dto.*;
import com.nhnacademy.inventory.medicines.medicine.dto.request.MedicineSearchRequest;
import com.nhnacademy.inventory.medicines.medicine.exception.*;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@Slf4j
class MedicineSearchServiceTest {

    @Mock
    MedicinePackageUnitRepository packageUnitRepository;

    @InjectMocks
    MedicineSearchService medicineSearchService;

    MedicineSearchRequest itemCodeRequest;
    MedicineSearchRequest productNameRequest;

    MedicinePackageSearchResponse response;
    MedicinePackageDetailResponse detailResponse;

    Pageable pageable;

    @BeforeEach
    void setUp(){


        productNameRequest = new MedicineSearchRequest(SearchType.PRODUCT_NAME," test ");
        itemCodeRequest = new MedicineSearchRequest(SearchType.ITEM_CODE,"123456789");
        pageable = Pageable.ofSize(10);
        response = new MedicinePackageSearchResponse(
                1L,
                1L,
                "123456789",
                "test-search",
                "test-company",
                "10ml"

        );
        detailResponse = new MedicinePackageDetailResponse(
                1L,
                1L,
                "123456789",
                "test-search",
                "test-company",
                "storage-test",
                "validity-test",
                "10ml",
                null
        );



    }


    @Test
    @DisplayName("제품명 조회")
    void searchByProductName() {


        Page<MedicinePackageSearchResponse> page = new PageImpl<>(List.of(response),pageable,1);



        given(packageUnitRepository.findAllWithMedicineByProductName(productNameRequest.search().trim(),pageable)).willReturn(page);

        Page<MedicinePackageSearchResponse> result = medicineSearchService.getMedicines(productNameRequest,pageable);

        verify(packageUnitRepository).findAllWithMedicineByProductName(productNameRequest.search().trim(),pageable);

        MedicinePackageSearchResponse test = result.getContent().getFirst();


        assertAll(
                () -> assertEquals(1,result.getTotalElements()),
                () -> assertEquals(1L, result.getTotalPages()),
                () -> assertEquals(1L,test.medicineId()),
                () -> assertEquals(1L,test.packageUnitId()),
                () -> assertEquals("123456789",test.itemCode()),
                () -> assertEquals("test-search",test.productName()),
                () -> assertEquals("test-company",test.companyName()),
                () -> assertEquals("10ml",test.packUnit())

        );



    }


    @Test
    @DisplayName("품목기준 코드 조회")
    void searchByItemCode(){


        given(packageUnitRepository.findAllWithMedicineByItemCode(itemCodeRequest.search(),pageable)).willReturn(new PageImpl<>(List.of(response)));
        Page<MedicinePackageSearchResponse> result = medicineSearchService.getMedicines(itemCodeRequest,pageable);
        verify(packageUnitRepository).findAllWithMedicineByItemCode("123456789",pageable);
        MedicinePackageSearchResponse test = result.getContent().getFirst();

        assertAll(
                () -> assertEquals(1,result.getTotalElements()),
                () -> assertEquals(1,result.getTotalPages()),
                () -> assertEquals(1L,test.medicineId()),
                () -> assertEquals(1L,test.packageUnitId()),
                () -> assertEquals("123456789",test.itemCode()),
                () -> assertEquals("test-search",test.productName()),
                () -> assertEquals("test-company",test.companyName()),
                () -> assertEquals("10ml",test.packUnit())


        );


    }


//            1L,
//            1L,
//            "123456789",
//            "test-search",
//            "test-company",
//            "storage-test",
//            "validity-test",
//            "10ml",
//            null



    @Test
    @DisplayName("의약품 상세 조회")
    void searchDetailMedicine() {


        given(packageUnitRepository.findDetailMedicine(1L)).willReturn(Optional.of(detailResponse));
        MedicinePackageDetailResponse result = medicineSearchService.getDetail(1L);
        verify(packageUnitRepository).findDetailMedicine(1L);

        assertAll(

                () -> assertEquals(1L,result.medicineId()),
                () -> assertEquals(1L,result.packageUnitId()),
                () -> assertEquals("123456789",result.itemCode()),
                () -> assertEquals("test-search",result.productName()),
                () -> assertEquals("test-company",result.companyName()),
                () -> assertEquals("storage-test",result.storageMethod()),
                () -> assertEquals("validity-test",result.validityPeriod()),
                () -> assertEquals("10ml",result.packUnit()),
                () -> assertNull(result.narcoticKindCode())

        );



    }



    @Test
    @DisplayName("제품명이나 품목기준코드 검색시 없는 정보 반환")
    void returnNullTest(){


        // 제품명 빈 리스트 반환
        given(packageUnitRepository.findAllWithMedicineByProductName("test",pageable)).willReturn(Page.empty(pageable));
        Page<MedicinePackageSearchResponse> result1 = medicineSearchService.getMedicines(productNameRequest,pageable);
        verify(packageUnitRepository).findAllWithMedicineByProductName("test",pageable);


        assertAll(

                () -> assertEquals(0,result1.getTotalPages()),
                () -> assertEquals(0,result1.getTotalElements()),
                () -> assertEquals(List.of(),result1.getContent()),
                () -> assertTrue(result1.isEmpty())

        );


        // 품목기준코드 빈 리스트 반환
        given(packageUnitRepository.findAllWithMedicineByItemCode("123456789",pageable)).willReturn(Page.empty(pageable));
        Page<MedicinePackageSearchResponse> result2 = medicineSearchService.getMedicines(itemCodeRequest,pageable);
        verify(packageUnitRepository).findAllWithMedicineByItemCode("123456789",pageable);

        assertAll(

                () -> assertEquals(0,result2.getTotalPages()),
                () -> assertEquals(0, result2.getTotalElements()),
                () -> assertEquals(List.of(),result2.getContent()),
                () -> assertTrue(result2.isEmpty())

        );



    }


    @Test
    @DisplayName("비정상 데이터")
    void errorTest(){

        // 품목기준코드가 숫자가 아닌 값
        MedicineSearchRequest codeRequest = new MedicineSearchRequest(SearchType.ITEM_CODE,"test-itemCode");
        assertThrows(ItemCodeInvalidException.class, () -> medicineSearchService.getMedicines(codeRequest,pageable));


        // 해당하는 특정 의약품이 없을 때

        given(packageUnitRepository.findDetailMedicine(1L)).willReturn(Optional.empty());
        assertThrows(PackUnitNotFoundException.class, () -> medicineSearchService.getDetail(1L));



    }





}