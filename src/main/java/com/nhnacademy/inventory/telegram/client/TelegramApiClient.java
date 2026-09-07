package com.nhnacademy.inventory.telegram.client;

import com.nhnacademy.inventory.telegram.config.TelegramProperties;
import com.nhnacademy.inventory.telegram.dto.TelegramApiResponse;
import com.nhnacademy.inventory.telegram.dto.TelegramSendMessageRequest;
import com.nhnacademy.inventory.telegram.dto.TelegramUpdate;
import com.nhnacademy.inventory.telegram.dto.TelegramUpdatesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Component
public class TelegramApiClient {

    private final RestClient restClient;
    private final TelegramProperties properties;

    public TelegramApiClient(
            @Qualifier("telegramRestClient") RestClient restClient,
            TelegramProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /**
     * offset 이후의 업데이트를 가져온다. 응답이 없으면 pollTimeoutSeconds 만큼 대기하다 빈 목록을 돌려준다.
     */
    public List<TelegramUpdate> getUpdates(Long offset) {
        try {
            TelegramUpdatesResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/bot{token}/getUpdates")
                            .queryParam("timeout", properties.pollTimeoutSeconds())
                            .queryParamIfPresent("offset", java.util.Optional.ofNullable(offset))
                            .build(properties.botToken()))
                    .retrieve()
                    .body(TelegramUpdatesResponse.class);

            if (response == null || !response.ok()) {
                log.warn("[Telegram] getUpdates 실패. description={}",
                        response == null ? null : response.description());
                return List.of();
            }

            return response.result() == null ? List.of() : response.result();
        } catch (RestClientException e) {
            log.warn("[Telegram] getUpdates 중 예외 발생", e);
            return List.of();
        }
    }

    public void sendMessage(String chatId, String text) {
        try {
            TelegramApiResponse response = restClient.post()
                    .uri("/bot{token}/sendMessage", properties.botToken())
                    .body(new TelegramSendMessageRequest(chatId, text))
                    .retrieve()
                    .body(TelegramApiResponse.class);

            if (response == null || !response.ok()) {
                log.warn("[Telegram] 메시지 발송 실패. chatId={}, description={}",
                        chatId, response == null ? null : response.description());
            }
        } catch (RestClientException e) {
            log.warn("[Telegram] 메시지 발송 중 예외 발생. chatId={}", chatId, e);
        }
    }
}
