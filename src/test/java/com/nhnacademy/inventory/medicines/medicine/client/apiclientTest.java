package com.nhnacademy.inventory.medicines.medicine.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiClientTest {

    @Autowired
    MedicineApiClient medicineApiClient;

    @Test
    void getJson() {


        medicineApiClient.getJson(1,50);


    }



}
