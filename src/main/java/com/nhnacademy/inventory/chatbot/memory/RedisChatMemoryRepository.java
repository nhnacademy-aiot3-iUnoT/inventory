package com.nhnacademy.inventory.chatbot.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

@Slf4j
@Repository
public class RedisChatMemoryRepository implements ChatMemoryRepository {

    private static final String KEY_PREFIX = "chat:memory:";
    private static final Duration TTL = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public RedisChatMemoryRepository(StringRedisTemplate redisTemplate, JsonMapper jsonMapper) {
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
    }

    // 대화 목록 전체 조회(KEYS 명령)은 Redis를 멈추게 할 수 있어서 사용하지 않음
    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        try {
            String raw = redisTemplate.opsForValue().get(key(conversationId));

            if (raw == null || raw.isBlank()) {
                return List.of();
            }

            List<StoredMessage> stored = jsonMapper.readValue(raw, new TypeReference<>() {});

            return stored.stream()
                    .map(StoredMessage::toMessage)
                    .toList();
        } catch (JacksonException e) {
            log.warn("대화 맥락을 읽지 못해 초기화합니다. conversationId={}", conversationId, e);
            deleteByConversationId(conversationId);
            return List.of();
        } catch (DataAccessException e) {
            log.warn("Redis에 접근하지 못해 맥락 없이 진행합니다.", e);
            return List.of();
        }
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        // 사용자 질문과 그에 대한 답변 텍스트만 저장함
        List<StoredMessage> stored = messages.stream()
                .filter(RedisChatMemoryRepository::isConversational)
                .map(StoredMessage::from)
                .toList();

        if (stored.isEmpty()) {
            deleteByConversationId(conversationId);
            return;
        }

        try {
            redisTemplate.opsForValue()
                    .set(key(conversationId), jsonMapper.writeValueAsString(stored), TTL);
        } catch (JacksonException | DataAccessException e) {
            log.warn("대화 맥락을 저장하지 못했습니다. conversationId={}", conversationId, e);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        try {
            redisTemplate.delete(key(conversationId));
        } catch (DataAccessException e) {
            log.warn("대화 맥락을 지우지 못했습니다. conversationId={}", conversationId, e);
        }
    }

    private static boolean isConversational(Message message) {
        MessageType type = message.getMessageType();

        // 도구 호출 중간 메시지(MessageType.TOOL)는 저장하지 않음
        boolean supported = (type == MessageType.USER || type == MessageType.ASSISTANT);

        boolean isNotNull = message.getText() != null && !message.getText().isBlank();

        return supported && isNotNull;
    }

    private String key(String conversationId) {
        return KEY_PREFIX + conversationId;
    }

    // Message는 인터페이스라 그대로 직렬화하면 타입 정보가 사라지므로 record로 저장
    private record StoredMessage(String type, String text) {

        private static StoredMessage from(Message message) {
            return new StoredMessage(message.getMessageType().name(), message.getText());
        }

        private Message toMessage() {
            return MessageType.ASSISTANT.name().equals(type)
                    ? new AssistantMessage(text)
                    : new UserMessage(text);
        }
    }
}
