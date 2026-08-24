package com.nhnacademy.inventory.medicines.medicine.client;

import com.nhnacademy.inventory.medicines.medicine.property.MedicineProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@Slf4j
public class MedicineApiClient {
    private final RestClient restClient;
    private final MedicineProperties medicineProperties;

    // 직접 생성자 주입 방법으로 수정
    public MedicineApiClient(
            @Qualifier("medicineRestClient") RestClient restClient,
            MedicineProperties medicineProperties
    ) {
        this.restClient = restClient;
        this.medicineProperties = medicineProperties;
    }

    public String getJson(int pageNo, int numOfRows){


       String json =  restClient.get()
                .uri(medicineProperties.detailPath() + "?serviceKey=" + medicineProperties.serviceKey() + "&pageNo=" + pageNo + "&numOfRows=" + numOfRows + "&type=json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().body(String.class);

       log.info("json: {}",json);

       return json;
    }



}
