package com.nhnacademy.inventory.medicines.medicine.repository;

import com.nhnacademy.inventory.global.config.QuerydslConfig;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QuerydslConfig.class)
class MedicineRepositoryTest {

    @Autowired
    MedicineRepository medicineRepository;


    @Test
    @DisplayName("품목기준코드가 있으면 true ")
    void existsByItemCode() {


        Medicine medicine = Medicine.create(
            "1234",
                "타이레놀",
                "test",
                "10개월",
                null,
                "보람 기업"
        );

        medicineRepository.save(medicine);


        assertTrue(medicineRepository.existsByItemCode("1234"));



    }


    @Test
    @DisplayName("품목기준코드가 없음 false")
    void notExistsByItemCode(){

        Medicine medicine = Medicine.create(
                "1234",
                "타이레놀",
                "test",
                "10개월",
                null,
                "보람 기업"
        );

        medicineRepository.save(medicine);


        assertFalse(medicineRepository.existsByItemCode("12345"));


    }






}