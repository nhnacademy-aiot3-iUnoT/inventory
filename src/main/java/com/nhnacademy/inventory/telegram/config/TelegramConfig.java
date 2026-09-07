package com.nhnacademy.inventory.telegram.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramConfig {

    @Bean("telegramRestClient")
    public RestClient telegramRestClient(TelegramProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.apiBaseUrl())
                .build();
    }
}
