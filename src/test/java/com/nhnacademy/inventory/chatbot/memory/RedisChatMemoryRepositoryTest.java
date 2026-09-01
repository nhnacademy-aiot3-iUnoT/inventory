package com.nhnacademy.inventory.chatbot.memory;

import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataRedisTest
@EnabledIf("redisAvailable")
class RedisChatMemoryRepositoryTest {

    private static final String CONVERSATION_ID = "test-conversation";
    private static final String KEY = "chat:memory:" + CONVERSATION_ID;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private RedisChatMemoryRepository repository;

    // Redis가 없으면 실패가 아니라 건너뜀
    static boolean redisAvailable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", 6379), 300);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        repository = new RedisChatMemoryRepository(redisTemplate, JsonMapper.builder().build());
        redisTemplate.delete(KEY);
    }

    @Test
    @DisplayName("사용자와 답변 메시지를 저장한 뒤 순서와 종류가 그대로 복원된다.")
    void saveAll_ThenFind_RoundTrips() {
        repository.saveAll(CONVERSATION_ID, List.of(
                new UserMessage("유통기한 임박한 거 알려줘"),
                new AssistantMessage("타이레놀정 외 2건이 있습니다.")));

        List<Message> found = repository.findByConversationId(CONVERSATION_ID);

        assertThat(found).hasSize(2);
        assertThat(found.get(0).getMessageType())
                .isEqualTo(MessageType.USER);
        assertThat(found.get(0).getText())
                .isEqualTo("유통기한 임박한 거 알려줘");
        assertThat(found.get(1).getMessageType())
                .isEqualTo(MessageType.ASSISTANT);
        assertThat(found.get(1).getText())
                .isEqualTo("타이레놀정 외 2건이 있습니다.");
    }

    @Test
    @DisplayName("다시 저장하면 이전 메시지를 남기지 않고 통째로 교체한다.")
    void saveAll_ReplacesPreviousMessages() {
        repository.saveAll(CONVERSATION_ID, List.of(new UserMessage("첫 번째 질문")));

        repository.saveAll(CONVERSATION_ID, List.of(new UserMessage("두 번째 질문")));

        List<Message> found = repository.findByConversationId(CONVERSATION_ID);

        assertThat(found)
                .hasSize(1);
        assertThat(found.getFirst().getText())
                .isEqualTo("두 번째 질문");
    }

    @Test
    @DisplayName("시스템 메시지와 본문이 빈 메시지는 저장하지 않는다.")
    void saveAll_FiltersNonConversationalMessages() {
        repository.saveAll(CONVERSATION_ID, List.of(
                new SystemMessage("당신은 재고 관리 챗봇입니다."),
                new UserMessage("재고 알려줘"),
                new AssistantMessage("")));

        List<Message> found = repository.findByConversationId(CONVERSATION_ID);

        assertThat(found)
                .hasSize(1);
        assertThat(found.getFirst().getText())
                .isEqualTo("재고 알려줘");
    }

    @Test
    @DisplayName("남길 메시지가 없으면 키를 지운다.")
    void saveAll_WhenNothingToKeep_DeletesKey() {
        repository.saveAll(CONVERSATION_ID, List.of(new UserMessage("재고 알려줘")));

        repository.saveAll(CONVERSATION_ID, List.of(new SystemMessage("프롬프트")));

        assertThat(repository.findByConversationId(CONVERSATION_ID))
                .isEmpty();
        assertThat(redisTemplate.hasKey(KEY))
                .isFalse();
    }

    @Test
    @DisplayName("만료 시간이 실제로 걸린다.")
    void saveAll_SetsExpiration() {
        repository.saveAll(CONVERSATION_ID, List.of(new UserMessage("재고 알려줘")));

        Long ttlSeconds = redisTemplate.getExpire(KEY);

        // 만료가 없으면 -1, 키가 없으면 -2를 돌려준다.
        assertThat(ttlSeconds)
                .isNotNull().
                isPositive()
                .isLessThanOrEqualTo(Duration.ofHours(1).toSeconds());
    }

    @Test
    @DisplayName("저장된 값이 없으면 빈 목록을 반환한다.")
    void findByConversationId_WhenAbsent_ReturnsEmpty() {
        assertThat(repository.findByConversationId("없는-대화"))
                .isEmpty();
    }

    @Test
    @DisplayName("값이 깨져 있으면 맥락을 버리고 키까지 정리한다.")
    void findByConversationId_WhenBroken_ClearsAndReturnsEmpty() {
        redisTemplate.opsForValue().set(KEY, "{망가진 값}");

        List<Message> found = repository.findByConversationId(CONVERSATION_ID);

        assertThat(found)
                .isEmpty();
        assertThat(redisTemplate.hasKey(KEY))
                .isFalse();
    }
}
