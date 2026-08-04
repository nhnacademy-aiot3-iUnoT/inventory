package com.nhnacademy.inventory.medicines.medicine.service;

import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicineResponse;
import com.nhnacademy.inventory.medicines.medicine.mapper.MedicineMapper;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicineRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineSaveServiceTest {

    @Mock
    MedicineRepository medicineRepository;
    @Mock
    MedicinePackageUnitRepository packageUnitRepository;
    @Mock
    MedicineMapper medicineMapper;

    @InjectMocks
    MedicineSaveService medicineSaveService;



    @Test
    @DisplayName("정상데이터 저장")
    void saveMedicines() {


        MedicineResponse response = new MedicineResponse(
                "1234",
                "medicine-test",
                "company-test",
                "storage-test",
                "validity-test",
                List.of("unit-test"),
                null
        );


        Medicine medicine = mock(Medicine.class);
        List<MedicinePackageUnit> packageUnits = List.of(mock(MedicinePackageUnit.class));

        given(medicineRepository.existsByItemCode("1234")).willReturn(false);
        given(medicineMapper.toMedicineEntity(response)).willReturn(medicine);
        given(medicineRepository.save(medicine)).willReturn(medicine);
        given(medicineMapper.toPackageUnitEntities(medicine,response)).willReturn(packageUnits);
        given(packageUnitRepository.saveAll(packageUnits)).willReturn(packageUnits);


        medicineSaveService.saveMedicines(List.of(response));
        verify(medicineRepository,times(1)).existsByItemCode("1234");
        verify(medicineMapper,times(1)).toMedicineEntity(response);
        verify(medicineRepository).save(any(Medicine.class));
        verify(medicineMapper,times(1)).toPackageUnitEntities(medicine,response);
        verify(packageUnitRepository).saveAll(packageUnits);


    }


    @Test
    @DisplayName("비정상데이터 무시")
    void nullTest(){

        // itemCode null일 때
        MedicineResponse nullResponse = new MedicineResponse(
                null,
                "medicine-test",
                "company-test",
                "storage-test",
                "validity-test",
                List.of("unit-test"),
                null
        );

        //
        // itemCode ''일때
        MedicineResponse blankResponse = new MedicineResponse(
                "",
                "medicine-test",
                "company-test",
                "storage-test",
                "validity-test",
                List.of("unit-test"),
                null
        );



        medicineSaveService.saveMedicines(List.of(nullResponse,blankResponse));




        verify(medicineMapper,never()).toMedicineEntity(nullResponse);
        verify(medicineMapper,never()).toMedicineEntity(blankResponse);
        verify(medicineRepository,never()).existsByItemCode(nullResponse.itemCode());
        verify(medicineRepository,never()).existsByItemCode(blankResponse.itemCode());
        verify(medicineRepository,never()).save(any(Medicine.class));
        verify(packageUnitRepository,never()).saveAll(anyList());


    }

    @Test
    @DisplayName("itemCode 이미 존재")
    void itemCodeTest(){


        MedicineResponse response = new MedicineResponse(
                "1234",
                "medicine-test",
                "company-test",
                "storage-test",
                "validity-test",
                List.of("unit-test"),
                null
        );


        given(medicineRepository.existsByItemCode("1234")).willReturn(true);

        medicineSaveService.saveMedicines(List.of(response));

        verify(medicineRepository).existsByItemCode("1234");
        verify(medicineMapper,never()).toMedicineEntity(any(MedicineResponse.class));
        verify(medicineMapper,never()).toPackageUnitEntities(any(Medicine.class),any(MedicineResponse.class));
        verify(medicineRepository,never()).save(any(Medicine.class));
        verify(packageUnitRepository,never()).saveAll(anyList());



    }



}