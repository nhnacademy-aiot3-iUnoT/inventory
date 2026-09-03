package com.nhnacademy.inventory.global.cache;

public record CacheInvalidationEvent(
        String cacheName,
        Object key
) {

    public static CacheInvalidationEvent of(String cacheName, Object key) {
        return new CacheInvalidationEvent(cacheName, key);
    }

    public static CacheInvalidationEvent clearAll(String cacheName) {
        return new CacheInvalidationEvent(cacheName, null);
    }
}
