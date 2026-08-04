package com.nhnacademy.inventory.medicines.medicine.config;

import com.nhnacademy.inventory.medicines.medicine.property.MedicineProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(MedicineProperties.class)
public class RestClientConfig {

    @Bean("medicineRestClient")
    public RestClient medicineRestClient(MedicineProperties medicineProperties){

        return RestClient.builder()
                .baseUrl(medicineProperties.baseUrl()).build();

    }


}
