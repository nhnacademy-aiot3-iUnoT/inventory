package com.nhnacademy.inventory.inventories.inventory.repository;

import com.nhnacademy.inventory.global.config.QuerydslConfig;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QuerydslConfig.class)
@Sql("/sql/inventory-test-data.sql")
class MedicineInventoryRepositoryImplTest {

    @Autowired
    MedicineInventoryRepositoryImpl medicineInventoryRepository;


    @Test
    @DisplayName("포장단위,구역,제조번호,유통기한 조회")
    void findByMedicinePackageUnitIdAndZoneIdAndLotNumberAndExpirationDateTest() {


        MedicineInventory medicineInventory = medicineInventoryRepository.findByMedicinePackageUnitIdAndZoneIdAndLotNumber(
                1L,
                1L,
                "LOT-TY-001"
        ).orElse(null);


        assertNotNull(medicineInventory);



        assertAll(

                () -> assertEquals(1L,medicineInventory.getId()),
                () -> assertEquals(1L,medicineInventory.getMedicinePackageUnit().getId()),
                () -> assertEquals(1L,medicineInventory.getZone().getId()),
                () -> assertEquals("LOT-TY-001",medicineInventory.getLotNumber()),
                () -> assertEquals(100,medicineInventory.getCurrentQuantity()),
                () -> assertEquals(ManagementStatus.NORMAL,medicineInventory.getManagementStatus())


        );


    }

    @Test
    @DisplayName("부서에 해당하는 의약품 조회")
    void findAllInventoriesByDepartmentIdsTest() {


        List<Long> departmentIds = List.of(1L);
        Pageable pageable = Pageable.ofSize(10);

        Page<InventoriesResponse> result =  medicineInventoryRepository.findAllInventoriesByDepartmentIds("타이레놀",1L,departmentIds,pageable);


        assertNotNull(result);
        assertEquals(2,result.getTotalElements());
        assertEquals(2,result.getContent().size());


        assertAll(

                () -> assertEquals(1L, result.getContent().getFirst().storageId()),
                () -> assertEquals(1L,result.getContent().getFirst().packUnitId()),
                () -> assertEquals("타이레놀정",result.getContent().getFirst().productName()),
                () -> assertEquals("001",result.getContent().getFirst().itemCode()),
                () -> assertEquals("10정",result.getContent().getFirst().packUnit()),
                () -> assertEquals(LocalDate.of(2027, Month.JANUARY,31),result.getContent().getFirst().expirationDate()),
                () -> assertEquals("약품창고 A", result.getContent().getFirst().storageName()),
                () -> assertEquals(100,result.getContent().getFirst().totalQuantity())

        );


    }

    @Test
    @DisplayName("전체 의약품 조회")
    void findAllInventoriesTest() {


        String search = "타이레놀";
        Long storageId = null;
        List<Long> storageIds = List.of(1L, 2L, 3L);
        Pageable pageable = Pageable.ofSize(10);

        Page<InventoriesResponse> result = medicineInventoryRepository.findAllInventories(
                        search,
                        storageId,
                        storageIds,
                        pageable
        );


        assertAll(
                () -> assertEquals(3, result.getContent().size()),

                () -> assertEquals(1L, result.getContent().getFirst().storageId()),
                () -> assertEquals(1L, result.getContent().getFirst().packUnitId()),
                () -> assertEquals("타이레놀정", result.getContent().getFirst().productName()),
                () -> assertEquals("001", result.getContent().getFirst().itemCode()),
                () -> assertEquals("10정", result.getContent().getFirst().packUnit()),
                () -> assertEquals(
                        LocalDate.of(2027, Month.JANUARY, 31),
                        result.getContent().getFirst().expirationDate()
                ),
                () -> assertEquals("약품창고 A", result.getContent().getFirst().storageName()),
                () -> assertEquals(100, result.getContent().getFirst().totalQuantity()),

                () -> assertEquals(1L, result.getContent().get(1).storageId()),
                () -> assertEquals(2L, result.getContent().get(1).packUnitId()),
                () -> assertEquals("타이레놀정", result.getContent().get(1).productName()),
                () -> assertEquals("001", result.getContent().get(1).itemCode()),
                () -> assertEquals("30정", result.getContent().get(1).packUnit()),
                () -> assertEquals(
                        LocalDate.of(2027, Month.MARCH, 31),
                        result.getContent().get(1).expirationDate()
                ),

                () -> assertEquals("약품창고 A", result.getContent().get(1).storageName()),
                () -> assertEquals(50, result.getContent().get(1).totalQuantity()),
                () -> assertEquals(3L, result.getContent().get(2).storageId()),
                () -> assertEquals(1L, result.getContent().get(2).packUnitId()),
                () -> assertEquals("타이레놀정", result.getContent().get(2).productName()),
                () -> assertEquals("001", result.getContent().get(2).itemCode()),
                () -> assertEquals("10정", result.getContent().get(2).packUnit()),
                () -> assertEquals(
                        LocalDate.of(2027, Month.MAY, 31),
                        result.getContent().get(2).expirationDate()
                ),

                () -> assertEquals("응급약품창고", result.getContent().get(2).storageName()),
                () -> assertEquals(70, result.getContent().get(2).totalQuantity())

        );


    }


    @Test
    @DisplayName("zoneIds, packUnitId로 의약품 상세 조회")
    void detailMedicineTest(){

        List<Long> zoneIds = List.of(1L);
        Long packUnitId = 1L;
        Pageable pageable = Pageable.ofSize(10);

        Page<InventoryResponse> result = medicineInventoryRepository.findByZonesAndPackUnitId(zoneIds,packUnitId,pageable);
        List<InventoryResponse> content = result.getContent();


        assertAll(

                () -> assertEquals(1L,content.getFirst().medicinePackUnitId()),
                () -> assertEquals(1L,content.getFirst().storageId()),
                () -> assertEquals(1L,content.getFirst().zoneId()),
                () -> assertEquals("LOT-TY-001",content.getFirst().lotNumber()),
                () -> assertEquals(LocalDate.of(2027,Month.JANUARY,31),content.getFirst().expirationDate()),
                () -> assertEquals(100,content.getFirst().currentQuantity()),
                () -> assertEquals(ManagementStatus.NORMAL,content.getFirst().managementStatus()),
                () -> assertEquals("타이레놀정",content.getFirst().productName()),
                () -> assertEquals("일반구역 A",content.getFirst().zoneName()),
                () -> assertEquals("약품창고 A",content.getFirst().storageName()),
                () -> assertEquals("001",content.getFirst().itemCode())


        );



    }


}