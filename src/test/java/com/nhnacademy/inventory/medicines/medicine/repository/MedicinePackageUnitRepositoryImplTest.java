package com.nhnacademy.inventory.medicines.medicine.repository;

import com.nhnacademy.inventory.global.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@Import(QuerydslConfig.class)
class MedicinePackageUnitRepositoryImplTest {


    @Autowired
    MedicinePackageUnitRepository repository;
    @Autowired
    TestEntityManager entityManager;


    @Test
    void findAllWithMedicineByProductName() {






    }

    @Test
    void findAllWithMedicineByItemCode() {
    }

    @Test
    void findDetailMedicine() {
    }
}