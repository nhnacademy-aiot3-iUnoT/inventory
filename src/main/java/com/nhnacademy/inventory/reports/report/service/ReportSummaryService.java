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
        1. 입력에 주어진 수치만 사용하세요. 비중은 괄호 안에 이미 계산되어 있으므로 직접 나누거나 더하지 마세요.
        2. 많다, 높다, 잦다 같은 표현을 쓸 때는 근거가 되는 수치를 반드시 함께 적으세요.
        3. 입력에 없는 값(유통기한, 안전재고, 발주 이력, 폐기 사유 등)을 사실처럼 쓰지 마세요.
           다만 그것을 확인하라고 권고하는 것은 가능합니다.
        4. 같은 의약품에 포장 단위가 여럿이면 단위별 사용량과 폐기량을 비교해 해석하세요.
        5. 기준을 벗어난 구역은 이탈이 일어난 날짜의 일별 수치를 근거로 드세요.
           같은 날 문 개폐 수치가 있으면 함께 언급하고, 주간 합계를 특정 날짜의 근거로 쓰지 마세요.
        6. 기준이 미설정인 항목은 이탈 여부를 판단하지 말고, 기준 설정을 권고하세요.
        7. 데이터가 '없음'인 항목은 그 절을 생략하세요.
        8. 관리자 권고사항은 최대 3개이며, 각각 대상과 행동과 근거가 드러나게 쓰세요.
           앞 절에서 이미 말한 내용을 반복하지 마세요.
        9. 아래 예시와 완전히 동일한 형식으로 작성하세요.

        출력 형식 예시:

        소비 동향
        A의약품 / 10정(PTP)이 사용량의 72.4%를 차지했습니다. 나머지 품목은 각각 10% 미만에 그쳐 소비가 특정 품목에 집중된 주간입니다.

        폐기 및 손실 분석
        폐기는 A의약품 / 500정(병)에 집중되어 폐기량의 68.0%를 차지했습니다.
        같은 의약품이라도 대용량 포장에서 폐기가 발생하고 있어 포장 단위별 발주 비중을 점검할 필요가 있습니다.

        보관 환경 분석
        구역 1의 온도가 7일 중 1일 기준 상한 8.0을 벗어났습니다. 이탈일인 08-12에 최고 9.9를 기록했고, 같은 날 문 개폐가 22회 45분으로 다른 날보다 많았습니다.
        개폐가 온도 상승에 영향을 주었을 가능성이 있습니다. 구역 2는 기준이 미설정이라 이탈 여부를 판단할 수 없습니다.

        관리자 권고사항
        A의약품 500정(병)의 폐기 사유를 확인하고, 10정(PTP) 대비 발주 비중 조정을 검토하십시오.
        구역 1은 08-12처럼 개폐가 몰리는 날의 출입 절차를 점검하십시오.
        구역 2의 온도 기준을 설정해 이탈 판단이 가능하도록 하십시오.

        예시의 품목명과 수치는 형식 참고용이며, 실제 데이터로 대체해서 작성합니다.
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
