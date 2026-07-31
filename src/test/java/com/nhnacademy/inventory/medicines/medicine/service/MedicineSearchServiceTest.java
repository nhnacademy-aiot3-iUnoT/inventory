package com.nhnacademy.inventory.medicines.medicine.service;

import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageDetailResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageSearchResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicineResponse;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
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



    @Test
    @DisplayName("저장 후 조회")
    void searchByProductName() {


        Pageable pageable = Pageable.ofSize(10);
        Medicine medicine = Medicine.builder()
                .itemCode("1234")
                .productName("product-test")
                .companyName("company-test")
                .storageMethod("storage-test")
                .validityPeriod("validity-test")
                .narcoticKindCode(null).build();


        MedicinePackageUnit medicinePackageUnit = MedicinePackageUnit.create(medicine,"unit-test");
        Page<MedicinePackageUnit> page = new PageImpl<>(List.of(medicinePackageUnit));

        given(packageUnitRepository.findAllWithMedicineByProductName("1234",pageable)).willReturn(page);

        Page<MedicinePackageSearchResponse> pageResponse = medicineSearchService.searchByProductName("1234",pageable);

        MedicinePackageSearchResponse searchResponse = pageResponse.getContent().getFirst();

        assertAll(

                () -> assertEquals("1234",searchResponse.itemCode()),
                () -> assertEquals("product-test",searchResponse.productName()),
                () -> assertEquals("company-test",searchResponse.companyName()),
                () -> assertEquals("unit-test",searchResponse.packUnit()),


                () -> assertEquals(1,pageResponse.getContent().size()),
                () -> assertEquals(1,pageResponse.getTotalElements()),
                () -> assertEquals(1,pageResponse.getTotalPages())


        );


        verify(packageUnitRepository,times(1)).findAllWithMedicineByProductName("1234",pageable);


    }


    @Test
    @DisplayName("의약품 상세 조회")
    void searchDetailMedicine() {


        Medicine medicine = Medicine.builder()
                .itemCode("1234")
                .productName("product-test")
                .companyName("company-test")
                .storageMethod("storage-test")
                .validityPeriod("validity-test")
                .narcoticKindCode(null)
                .build();

        MedicinePackageUnit medicinePackageUnit = MedicinePackageUnit.create(medicine,"pack-test");


        ReflectionTestUtils.setField(medicine,"id",1L);
        ReflectionTestUtils.setField(medicinePackageUnit,"id",1L);
        log.info("medicine : {}",medicine.getId());
        log.info("medicinePackageUnit: {}",medicinePackageUnit.getId());

        given(packageUnitRepository.findWithMedicineByPackUnitId(1L)).willReturn(Optional.of(medicinePackageUnit));

        MedicinePackageDetailResponse result = medicineSearchService.searchDetailMedicine(1L);

        assertEquals("1234",medicine.getItemCode());
        assertEquals("product-test",medicine.getProductName());
        assertEquals("company-test",medicine.getCompanyName());
        assertEquals("storage-test",medicine.getStorageMethod());








    }
}