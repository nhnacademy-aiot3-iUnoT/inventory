package com.nhnacademy.inventory.telegram.service;

import com.nhnacademy.inventory.telegram.client.TelegramApiClient;
import com.nhnacademy.inventory.telegram.dto.TelegramUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "chatbot.telegram", name = "enabled", havingValue = "true")
public class TelegramUpdatePoller {

    private final TelegramApiClient telegramApiClient;
    private final TelegramUpdateHandler updateHandler;
    
    private volatile Long offset;

    @Scheduled(fixedDelay = 1000L)
    @SchedulerLock(
            name = "telegramUpdatePoller",
            lockAtLeastFor = "PT1S",
            lockAtMostFor = "PT2M"
    )
    public void poll() {
        List<TelegramUpdate> updates = telegramApiClient.getUpdates(offset);

        if (updates.isEmpty()) {
            return;
        }

        long maxUpdateId = 0L;

        for (TelegramUpdate update : updates) {
            if (update.updateId() == null) {
                continue;
            }

            maxUpdateId = Math.max(maxUpdateId, update.updateId());

            try {
                updateHandler.handle(update);
            } catch (Exception e) {
                // 한 건이 실패해도 offset은 진행시킨다. 그렇지 않으면 같은 메시지를 무한히 다시 받는다.
                log.error("[Telegram] 메시지 처리 실패. updateId={}", update.updateId(), e);
            }
        }

        if (maxUpdateId > 0L) {
            offset = maxUpdateId + 1;
        }
    }
}
