package com.nhnacademy.inventory.assistant.service;

import com.nhnacademy.inventory.assistant.rule.Finding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssistantNarrator {

    // 엮을 사실이 둘 이상일 때만 LLM 을 씀
    private static final int LLM_THRESHOLD = 2;

    private static final String PROMPT = """
        재고 관리자에게 보낼 짧은 안내문을 작성합니다.
        입력은 같은 의약품 하나에 대해 시스템이 감지한 사항들입니다.

        작성 규칙:
        1. 입력에 있는 사실만 쓰세요. 수치와 날짜는 그대로 옮기세요.
        2. 입력에 없는 조치나 보관 규정을 지어내지 마세요.
        3. 여러 사항을 한 문단으로 잇되, 요약한다고 정보를 버리지 마세요.
        4. 의약품명과 구역명은 화면이 따로 표시하므로 문장에서 반복하지 마세요.
        5. 300자 이내, 존댓말 평서문으로 쓰고 별표, 샵, 목록 기호는 쓰지 마세요.
        """;

    private final ChatClient chatClient;

    public AssistantNarrator(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(PROMPT)
                .build();
    }

    public String describe(List<Finding> findings) {
        String explanations = explanations(findings);

        // 하나면 규칙이 만든 문장이 이미 완결돼 있음
        if (findings.size() < LLM_THRESHOLD) {
            return explanations;
        }

        try {
            String narrated = chatClient.prompt()
                    .user(explanations)
                    .call()
                    .content();

            return (narrated == null || narrated.isBlank()) ? explanations : narrated.trim();
        } catch (Exception e) {
            log.warn("[Assistant] 안내문 생성 실패. 판정 결과를 그대로 알립니다.", e);

            return explanations;
        }
    }

    private String explanations(List<Finding> findings) {
        return findings.stream()
                .map(Finding::explanation)
                .collect(Collectors.joining("\n"));
    }
}
