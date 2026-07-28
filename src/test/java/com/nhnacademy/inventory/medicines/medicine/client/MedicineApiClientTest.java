package com.nhnacademy.inventory.medicines.medicine.client;


import com.nhnacademy.inventory.medicines.medicine.property.MedicineProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.springframework.http.HttpMethod;

import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


class MedicineApiClientTest {

    private MedicineApiClient medicineApiClient;
    private MockRestServiceServer mockServer;



    @BeforeEach
    void setUp(){

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        MedicineProperties medicineProperties = mock(MedicineProperties.class);

        when(medicineProperties.detailPath()).thenReturn("/medicine-test");
        when(medicineProperties.serviceKey()).thenReturn("serviceKey-test");
        when(medicineProperties.baseUrl()).thenReturn("http://localhost");

        RestClient restClient = builder.baseUrl(medicineProperties.baseUrl()).build();

        medicineApiClient = new MedicineApiClient(restClient,medicineProperties);



    }



    @Test
    void getMedicines() {

        String responseBody = """
                
                {
                    "body": {
                        "totalCount": 1,
                        "items": [
                            {
                            "ITEM_SEQ": "12345",
                            "ITEM_NAME": 의약품 테스트
                            }
                       ]

                    }

                }
                """;

        mockServer.expect(requestTo("http://localhost/medicine-test?serviceKey=serviceKey-test"+"&pageNo=1"+ "&numOfRows=500"+"&type=json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        String json = medicineApiClient.getJson(1,500);
        assertEquals(responseBody,json);

        mockServer.verify();


    }
}