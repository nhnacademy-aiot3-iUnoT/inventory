package com.nhnacademy.inventory.telegram.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param enabled            폴링 사용 여부. 로컬에서 봇 토큰 없이 띄울 때 끄기 위한 스위치다.
 * @param botToken           봇 토큰
 * @param apiBaseUrl         텔레그램 API 주소
 * @param pollTimeoutSeconds getUpdates 롱폴링 대기 시간
 * @param commandPrefix      이 접두사로 시작하는 메시지만 챗봇에 전달한다.
 */
@ConfigurationProperties("chatbot.telegram")
public record TelegramProperties(
        boolean enabled,
        String botToken,
        String apiBaseUrl,
        int pollTimeoutSeconds,
        String commandPrefix
) {
    public TelegramProperties {
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            apiBaseUrl = "https://api.telegram.org";
        }
        if (pollTimeoutSeconds <= 0) {
            pollTimeoutSeconds = 30;
        }
        if (commandPrefix == null || commandPrefix.isBlank()) {
            commandPrefix = "/재고";
        }
    }
}
