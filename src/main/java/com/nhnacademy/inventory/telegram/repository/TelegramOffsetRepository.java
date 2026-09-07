package com.nhnacademy.inventory.telegram.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TelegramOffsetRepository {

    private static final String KEY = "telegram:update:offset";

    private final StringRedisTemplate redisTemplate;

    public Optional<Long> find() {
        String raw = redisTemplate.opsForValue().get(KEY);

        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.valueOf(raw.trim()));
        } catch (NumberFormatException e) {
            log.warn("[Telegram] offset 값이 올바르지 않아 무시합니다. value={}", raw, e);
            return Optional.empty();
        }
    }

    public void save(long offset) {
        try {
            redisTemplate.opsForValue().set(KEY, String.valueOf(offset));
        } catch (DataAccessException e) {
            log.warn("[Telegram] offset을 저장하지 못했습니다. offset={}", offset, e);
        }
    }
}
