package com.nhnacademy.inventory.global.config;

import com.nhnacademy.inventory.medicines.medicine.property.MedicineProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(MedicineProperties.class)
public class RestClientConfig {

    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean("medicineRestClient")
    public RestClient medicineRestClient(MedicineProperties medicineProperties) {
        return RestClient.builder()
                .baseUrl(medicineProperties.baseUrl())
                .build();
    }

    @Bean("loadBalancedRestClientBuilder")
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean("accountRestClient")
    @Profile("prod")
    public RestClient prodAccountRestClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder,
            @Value("${clients.account.base-url}") String baseUrl
    ) {
        // Builder는 가변 객체라 빈을 공유하면 나중에 설정된 baseUrl이 덮어씀.
        return builder.clone()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("accountRestClient")
    @Profile("!prod")
    public RestClient localAccountRestClient(@Value("${clients.account.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("ruleEngineRestClient")
    @Profile("prod")
    public RestClient prodRuleEngineRestClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder,
            @Value("${clients.rule-engine.base-url}") String baseUrl
    ) {
        return builder.clone()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("ruleEngineRestClient")
    @Profile("!prod")
    public RestClient localRuleEngineRestClient(@Value("${clients.rule-engine.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
