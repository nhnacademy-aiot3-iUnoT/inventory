package com.nhnacademy.inventory.medicines.medicine.service;

import com.nhnacademy.inventory.medicines.medicine.client.MedicineApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MedicineApiServiceTest {

    @Autowired
    MedicineApiService medicineApiService;


    @Test
    void getAllMedicines() {

        medicineApiService.getAllMedicines();


    }

}