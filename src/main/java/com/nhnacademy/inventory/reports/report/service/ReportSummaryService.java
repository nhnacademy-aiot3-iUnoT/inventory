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
        당신은 병원 의약품 재고 관리 담당자를 위한 주간 리포트를 작성합니다.
    
        작성 규칙:
        1. 단순 숫자 나열을 지양하고, 관리 관점의 인사이트(소비 집중도, 사용량 대비 폐기 비중 등)를 도출하세요.
        2. 주어진 데이터 범위 내에서만 해석하고, 없는 정보를 지어내지 마세요.
        3. 아래 예시와 완전히 동일한 형식으로 작성하세요.
    
        출력 형식 예시:
    
        소비 동향
        A의약품(10정)이 전체 출고량의 대부분을 차지했습니다. 나머지 품목은 소량에 그쳐 소비가 특정 품목에 집중된 주간이었습니다.
    
        폐기 및 손실 분석
        폐기는 A의약품(10정)에 집중되었으며, 해당 품목 출고량 대비 비중이 높습니다. 유통기한 관리 점검이 필요합니다.
    
        관리자 권고사항
        A의약품(10정)의 안전재고를 우선 점검하고, 폐기 사유를 확인해 발주량 조정을 검토하십시오.
    
        예시의 품목명과 내용은 형식 참고용이며, 실제 데이터로 대체해서 작성합니다.
        제목과 문장만 쓰고 별표, 샵, 대괄호는 사용하지 않습니다.
        """;

    public String generateSummary(String reportText) {
        String summary = chatClientBuilder.build()
                .prompt()
                .system(PROMPT)
                .user(reportText)
                .call()
                .content();

        log.info("생성된 리포트 요약: {}", summary);

        return summary;
    }
}
