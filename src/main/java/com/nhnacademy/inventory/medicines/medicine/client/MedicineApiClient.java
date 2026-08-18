package com.nhnacademy.inventory.medicines.medicine.client;

import com.nhnacademy.inventory.medicines.medicine.property.MedicineProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@Slf4j
@RequiredArgsConstructor
public class MedicineApiClient {
//    @Qualifier("medicineRestClient")
    private final RestClient restClient;
    private final MedicineProperties medicineProperties;

    public String getJson(int pageNo, int numOfRows){


       String json =  restClient.get()
                .uri(medicineProperties.detailPath() + "?serviceKey=" + medicineProperties.serviceKey() + "&pageNo=" + pageNo + "&numOfRows=" + numOfRows + "&type=json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().body(String.class);

       log.info("json: {}",json);

       return json;
    }



}
