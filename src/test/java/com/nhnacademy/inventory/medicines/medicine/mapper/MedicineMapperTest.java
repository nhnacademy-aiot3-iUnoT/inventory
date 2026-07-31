package com.nhnacademy.inventory.medicines.medicine.mapper;

import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicineResponse;
import com.nhnacademy.inventory.medicines.medicine.exception.CompanyNameRequiredException;
import com.nhnacademy.inventory.medicines.medicine.exception.ItemCodeRequiredException;
import com.nhnacademy.inventory.medicines.medicine.exception.MedicineRequiredException;
import com.nhnacademy.inventory.medicines.medicine.exception.ProductNameRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MedicineMapperTest {

    private MedicineMapper medicineMapper;

    @BeforeEach
    void setUp(){

        medicineMapper = new MedicineMapper();

    }


    @Test
    @DisplayName("정상 데이터 테스트")
    void medicineMapperTest() {

        MedicineResponse medicineResponse = new MedicineResponse(
                "1234",
                "product-test",
                "companyName-test",
                "storage-test",
                "validity-test",
                List.of("units-test"),
                null

        );


        Medicine medicine = medicineMapper.toMedicineEntity(medicineResponse);
        List<MedicinePackageUnit> packageUnits = medicineMapper.toPackageUnitEntities(medicine,medicineResponse);

        assertAll(
                () -> assertEquals("1234",medicine.getItemCode()),
                () -> assertEquals("product-test",medicine.getProductName()),
                () -> assertEquals("companyName-test",medicine.getCompanyName()),
                () -> assertEquals("storage-test",medicine.getStorageMethod()),
                () -> assertEquals("validity-test",medicine.getValidityPeriod()),
                () -> assertNull(medicine.getNarcoticKindCode()),
                () -> assertEquals(medicine,packageUnits.getFirst().getMedicine()),
                () -> assertEquals("units-test",packageUnits.getFirst().getPackUnit())

        );


    }

    @Test
    @DisplayName("비정상 데이터 테스트")
    void medicineErrorTest(){

        //itemCode null
        MedicineResponse response1 = new MedicineResponse(
                null,
                "product-test",
                "companyName-test",
                "storage-test",
                "validity-test",
                List.of("units-test"),
                null

        );

        // productName null
        MedicineResponse response2 = new MedicineResponse(
                "1234",
                null,
                "companyName-test",
                "storage-test",
                "validity-test",
                List.of("units-test"),
                null

        );


        // companyName null
        MedicineResponse response3 = new MedicineResponse(
                "1234",
                "product-test",
                null,
                "storage-test",
                "validity-test",
                List.of("units-test"),
                null

        );

        // 포장 단위 null
        MedicineResponse response4 = new MedicineResponse(
                "1234",
                "product-test",
                "companyName-test",
                "storage-test",
                "validity-test",
                List.of(),
                null
        );




        // 정상
        MedicineResponse medicineResponse = new MedicineResponse(
                "1234",
                "product-test",
                "companyName-test",
                "storage-test",
                "validity-test",
                List.of("units-test"),
                null

        );


        Medicine medicine = medicineMapper.toMedicineEntity(medicineResponse);


        assertAll(

                () -> assertThrows(ItemCodeRequiredException.class, () -> medicineMapper.toMedicineEntity(response1)),
                () -> assertThrows(ProductNameRequiredException.class, () -> medicineMapper.toMedicineEntity(response2)),
                () -> assertThrows(CompanyNameRequiredException.class, () -> medicineMapper.toMedicineEntity(response3)),
                () -> assertThrows(MedicineRequiredException.class, () -> medicineMapper.toPackageUnitEntities(null,medicineResponse)),
                () -> assertEquals(List.of(),medicineMapper.toPackageUnitEntities(medicine,response4))


        );







    }



}