package com.nhnacademy.inventory.medicines.medicine.client;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;


class MedicineApiClientTest {

    private MedicineApiClient medicineApiClient;
    private MockRestServiceServer mockServer;



    @BeforeEach
    void setUp(){

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();





    }



    @Test
    void getMedicines() {

        medicineApiClient.getJson(1,10);



    }
}