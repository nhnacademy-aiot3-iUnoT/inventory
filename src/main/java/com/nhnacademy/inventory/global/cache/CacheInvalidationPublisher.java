package com.nhnacademy.inventory.global.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final FanoutExchange cacheExchange;

    public void publish(String cacheName, Object key) {
        try {
            String payload = objectMapper.writeValueAsString(
                    new CacheInvalidationMessage(cacheName,
                            key == null ? null : key.toString(), Instant.now()));
            rabbitTemplate.convertAndSend(cacheExchange.getName(), "", payload);
        } catch (Exception e) {

            log.error("캐시 무효화 이벤트 발행 실패. cache={}, key={}", cacheName, key, e);
        }
    }
}