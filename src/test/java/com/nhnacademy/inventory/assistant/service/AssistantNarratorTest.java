package com.nhnacademy.inventory.assistant.service;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.FindingType;
import com.nhnacademy.inventory.assistant.rule.TargetReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssistantNarratorTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private AssistantNarrator narrator;

    @BeforeEach
    void setUp() {
        given(chatClientBuilder.defaultSystem(anyString())).willReturn(chatClientBuilder);
        given(chatClientBuilder.build()).willReturn(chatClient);

        narrator = new AssistantNarrator(chatClientBuilder);
    }

    @Test
    @DisplayName("판정이 하나면 LLM을 부르지 않고 그 설명을 그대로 쓴다.")
    void describe_WhenSingleFinding_SkipsLlm() {
        String described = narrator.describe(List.of(finding("첫 번째 설명입니다.")));

        assertThat(described).isEqualTo("첫 번째 설명입니다.");
        then(chatClient).should(never()).prompt();
    }

    @Test
    @DisplayName("판정이 여럿이면 LLM이 엮은 문장을 쓴다.")
    void describe_WhenMultipleFindings_UsesLlm() {
        givenNarrated("두 사항을 엮은 문장입니다.");

        String described = narrator.describe(List.of(finding("설명 하나"), finding("설명 둘")));

        assertThat(described).isEqualTo("두 사항을 엮은 문장입니다.");
    }

    @Test
    @DisplayName("LLM 호출이 실패하면 규칙이 만든 설명을 그대로 알린다.")
    void describe_WhenLlmFails_FallsBackToFindings() {
        given(chatClient.prompt().user(anyString()).call().content())
                .willThrow(new RuntimeException("AI 서비스 장애"));

        String described = narrator.describe(List.of(finding("설명 하나"), finding("설명 둘")));

        assertThat(described).contains("설명 하나").contains("설명 둘");
    }

    @Test
    @DisplayName("LLM이 빈 문장을 돌려주면 규칙이 만든 설명을 알린다.")
    void describe_WhenLlmReturnsBlank_FallsBackToFindings() {
        givenNarrated("   ");

        String described = narrator.describe(List.of(finding("설명 하나"), finding("설명 둘")));

        assertThat(described).contains("설명 하나").contains("설명 둘");
    }

    private void givenNarrated(String narrated) {
        given(chatClient.prompt().user(anyString()).call().content()).willReturn(narrated);
    }

    private Finding finding(String explanation) {
        return new Finding(
                FindingType.EXPIRY_ORDER, Severity.WARN, "타이레놀정", "구역1 입고", explanation,
                new TargetReference(TargetType.PACK_UNIT, 1L, 3L));
    }
}
