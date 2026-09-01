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

    private static final int LLM_THRESHOLD = 2;

    private static final String PROMPT = """
        당신은 의약품 재고 관리자를 돕는 비서입니다.
        시스템이 방금 감지한 사항을 사용자에게 알리는 짧은 안내문을 작성합니다.

        작성 규칙:
        1. 입력에 있는 사실만 쓰세요. 수치를 바꾸거나 새로 만들지 마세요.
        2. 의약품명, 구역명, 로트번호, 날짜, 수량은 하나도 빠뜨리지 말고 그대로 옮기세요.
        3. 입력에 없는 조치나 보관 규정을 지어내지 마세요.
        4. 여러 사항을 한 문단으로 잇되, 요약한다고 정보를 버리지 마세요.
        5. 300자 이내, 존댓말 평서문으로 쓰고 별표, 샵, 목록 기호는 쓰지 마세요.
        """;

    private final ChatClient chatClient;

    public AssistantNarrator(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(PROMPT)
                .build();
    }

    public String describe(List<Finding> findings) {
        String facts = facts(findings);

        if (findings.size() < LLM_THRESHOLD) {
            return facts;
        }

        try {
            String narrated = chatClient.prompt()
                    .user(facts)
                    .call()
                    .content();

            return (narrated == null || narrated.isBlank()) ? facts : narrated.trim();
        } catch (Exception e) {
            log.warn("[Assistant] 안내문 생성 실패. 판정 결과를 그대로 알립니다.", e);

            return facts;
        }
    }

    private String facts(List<Finding> findings) {
        return findings.stream()
                .map(Finding::describe)
                .collect(Collectors.joining("\n"));
    }
}
