package com.nhnacademy.inventory.reports.report.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReportSummaryServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    @InjectMocks
    private ReportSummaryService reportSummaryService;

    @Test
    @DisplayName("리포트 생성 중 예외가 발생하면 빈 문자열을 반환한다.")
    void generateSummary_WhenOccurException_ReturnsEmptyString() {
        // given
        given(chatClientBuilder.build()
                .prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .content())
                .willThrow(new RuntimeException("API 에러"));

        // when
        String result = reportSummaryService.generateSummary("텍스트");

        // then
        assertThat(result)
                .isEmpty();
    }
}