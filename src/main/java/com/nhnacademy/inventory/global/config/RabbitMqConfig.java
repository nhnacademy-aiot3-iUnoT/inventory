package com.nhnacademy.inventory.global.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String CACHE_EXCHANGE = "iunot.cache_exchange";
    
    @Value("${inventory.rabbitmq.exchange-suffix:}")
    private String exchangeSuffix;

    @Bean
    public FanoutExchange cacheExchange() {
        return new FanoutExchange(CACHE_EXCHANGE + exchangeSuffix, true, false);
    }
}