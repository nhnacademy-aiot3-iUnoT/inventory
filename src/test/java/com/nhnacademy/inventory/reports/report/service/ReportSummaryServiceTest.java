package com.nhnacademy.inventory.reports.report.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReportSummaryServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private ReportSummaryService reportSummaryService;

    // ChatClient를 생성자에서 한 번만 만들므로, 만들어진 결과를 목으로 바꿔치기한 뒤 검증한다.
    @BeforeEach
    void setUp() {
        given(chatClientBuilder.defaultSystem(anyString())).willReturn(chatClientBuilder);
        given(chatClientBuilder.build()).willReturn(chatClient);

        reportSummaryService = new ReportSummaryService(chatClientBuilder);
    }

    @Test
    @DisplayName("리포트 생성 중 예외가 발생하면 예외 처리를 유스케이스로 위임한다.")
    void generateSummary_WhenOccurException_DoesNothing() {
        // given
        given(chatClient.prompt()
                .user(anyString())
                .call()
                .content())
                .willThrow(new RuntimeException("API 에러"));

        // when & then
        assertThatThrownBy(() -> reportSummaryService.generateSummary("텍스트"))
                .isInstanceOf(RuntimeException.class);
    }
}
