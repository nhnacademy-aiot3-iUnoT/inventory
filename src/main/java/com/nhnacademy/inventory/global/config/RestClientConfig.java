package com.nhnacademy.inventory.global.config;

import com.nhnacademy.inventory.medicines.medicine.property.MedicineProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(MedicineProperties.class)
public class RestClientConfig {

    @Bean("medicineRestClient")
    public RestClient medicineRestClient(MedicineProperties medicineProperties) {
        return RestClient.builder()
                .baseUrl(medicineProperties.baseUrl())
                .build();
    }

    @Bean("loadBalancedAccountRestClientBuilder")
    @LoadBalanced
    @Profile("prod")
    public RestClient.Builder loadBalancedAccountRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean("accountRestClient")
    @Profile("prod")
    public RestClient prodAccountRestClient(
            @Qualifier("loadBalancedAccountRestClientBuilder") RestClient.Builder builder,
            @Value("${clients.account.base-url}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean("accountRestClient")
    @Profile("!prod")
    public RestClient localAccountRestClient(@Value("${clients.account.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
