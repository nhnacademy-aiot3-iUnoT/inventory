package com.nhnacademy.inventory.telegram.service;

import com.nhnacademy.inventory.telegram.client.TelegramApiClient;
import com.nhnacademy.inventory.telegram.dto.TelegramUpdate;
import com.nhnacademy.inventory.telegram.repository.TelegramOffsetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
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
    private final TelegramOffsetRepository offsetRepository;

    @Scheduled(fixedDelay = 1000L)
    @SchedulerLock(
            name = "telegramUpdatePoller",
            lockAtLeastFor = "PT1S",
            lockAtMostFor = "PT2M"
    )
    public void poll() {
        Long offset;

        // offset은 여러 인스턴스가 공유해야 한다. 읽지 못한 채로 폴링하면 이미 답한 메시지를 다시 답하게 되므로
        // 이번 주기는 건너뛰고 다음 주기에 다시 시도한다.
        try {
            offset = offsetRepository.find().orElse(null);
        } catch (DataAccessException e) {
            log.warn("[Telegram] offset을 읽지 못해 이번 폴링을 건너뜁니다.", e);
            return;
        }

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
            offsetRepository.save(maxUpdateId + 1);
        }
    }
}
