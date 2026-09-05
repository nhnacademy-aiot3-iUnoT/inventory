package com.nhnacademy.inventory.telegram.service;

import com.nhnacademy.inventory.telegram.client.TelegramApiClient;
import com.nhnacademy.inventory.telegram.config.TelegramProperties;
import com.nhnacademy.inventory.telegram.dto.TelegramChat;
import com.nhnacademy.inventory.telegram.dto.TelegramMessage;
import com.nhnacademy.inventory.telegram.dto.TelegramUpdate;
import com.nhnacademy.inventory.organizations.department.domain.Department;
import com.nhnacademy.inventory.organizations.notification.exception.DepartmentTelegramChatNotFoundException;
import com.nhnacademy.inventory.organizations.notification.service.DepartmentTelegramChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramUpdateHandler {

    private static final String CONVERSATION_ID_PREFIX = "telegram:";

    private final DepartmentTelegramChatService departmentTelegramChatService;
    private final TelegramChatbotService telegramChatbotService;
    private final TelegramApiClient telegramApiClient;
    private final TelegramProperties properties;

    public void handle(TelegramUpdate update) {
        TelegramMessage message = update.message();
        if (message == null || message.text() == null || message.chat() == null) {
            return;
        }

        TelegramChat chat = message.chat();
        // 개인 DM chatbot 동작하지 않는다
        if (!chat.isGroup()) {
            return;
        }

        // /chatbot 명령어로 시작하는 명령만 처리한다
        String question = extractQuestion(message.text());
        if (question == null) {
            return;
        }

        String chatId = String.valueOf(chat.id());

        Department department;
        try {
            department = departmentTelegramChatService.resolveDepartmentByChatId(chatId);
        } catch (DepartmentTelegramChatNotFoundException e) {
            log.info("[Telegram] 연결되지 않은 단톡방의 메시지. chatId={}", chatId);

            telegramApiClient.sendMessage(chatId, """
                    이 단톡방에 연결된 부서가 없습니다.
                    관리자에게 아래 번호로 등록을 요청해주세요.

                    단톡방 번호: %s""".formatted(chatId));
            return;
        }

        String answer = telegramChatbotService.answer(
                department.getId(),
                CONVERSATION_ID_PREFIX + chatId,
                question
        );

        telegramApiClient.sendMessage(chatId, answer);
    }


    private String extractQuestion(String text) {
        String trimmed = text.trim();
        String prefix = properties.commandPrefix();

        if (!trimmed.startsWith(prefix)) {
            return null;
        }

        String question = trimmed.substring(prefix.length()).trim();

        return question.isEmpty() ? null : question;
    }
}
