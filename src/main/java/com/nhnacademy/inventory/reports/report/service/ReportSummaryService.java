package com.nhnacademy.inventory.reports.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportSummaryService {

    private final ChatClient.Builder chatClientBuilder;
    private static final String PROMPT = """
        당신은 의약품 보관 관리 시스템의 리포트를 작성합니다.
        아래는 해당 기간에 실제로 집계된 수치입니다.
        
        규칙:
        - 주어진 수치만 이용하세요. 없는 정보를 추론하거나 지어내지 마세요.
        - 모든 항목을 나열하지 마세요. 눈에 띄는 것 2~3개만 언급합니다.
        - 가능하면 항목 간 관계를 짚으세요 (예: 특정 품목에 사용이 집중됨, 폐기 사유가 한쪽에 몰림).
        - 3~4문장의 평문으로 작성하고, 강조 문법같은 특수 문법을 사용하지 않습니다.
        """;

    public String generateSummary(String reportText) {

        try {
            return chatClientBuilder.build()
                    .prompt()
                    .system(PROMPT)
                    .user(reportText)
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("리포트 AI 요약 생성 실패", e);
            return "";
        }
    }
}
