package com.nhnacademy.inventory.global.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CacheInvalidationEventListener {

    private final CacheInvalidationPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onCacheInvalidation(CacheInvalidationEvent event) {
        publisher.publish(event.cacheName(), event.key());
    }
}
