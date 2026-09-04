package com.nhnacademy.inventory.global.cache;

import java.time.Instant;

/**
 * 캐시 무효화 메시지. key 가 null 이면 해당 캐시 전체를 비우라는 뜻이다.
 */
public record CacheInvalidationMessage(
        String cacheName,
        String key,
        Instant occurredAt
) {
}
