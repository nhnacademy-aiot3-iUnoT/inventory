package com.nhnacademy.inventory.chatbot.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

// Redis가 죽었을 때 챗봇이 멈추지 않는지 확인하는 테스트
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisChatMemoryRepositoryFailureTest {

    private static final String CONVERSATION_ID = "conv-1";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisChatMemoryRepository repository;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue())
                .willReturn(valueOperations);

        RedisConnectionFailureException failure =
                new RedisConnectionFailureException("Redis 연결 실패");

        given(valueOperations.get(anyString()))
                .willThrow(failure);
        willThrow(failure)
                .given(valueOperations)
                .set(anyString(), anyString(), any(Duration.class));
        given(redisTemplate.delete(anyString()))
                .willThrow(failure);

        repository = new RedisChatMemoryRepository(redisTemplate, JsonMapper.builder().build());
    }

    @Test
    @DisplayName("Redis 조회가 실패하면 예외 대신 빈 맥락을 돌려준다.")
    void findByConversationId_WhenRedisDown_ReturnsEmpty() {
        assertThat(repository.findByConversationId(CONVERSATION_ID))
                .isEmpty();
    }

    @Test
    @DisplayName("Redis 저장이 실패해도 예외를 밖으로 던지지 않는다.")
    void saveAll_WhenRedisDown_DoesNotThrow() {
        assertThatCode(() -> repository.saveAll(CONVERSATION_ID, List.of(new UserMessage("재고 알려줘"))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Redis 삭제가 실패해도 예외를 밖으로 던지지 않는다.")
    void deleteByConversationId_WhenRedisDown_DoesNotThrow() {
        assertThatCode(() -> repository.deleteByConversationId(CONVERSATION_ID))
                .doesNotThrowAnyException();
    }
}
