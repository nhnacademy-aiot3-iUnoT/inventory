package com.nhnacademy.inventory.chatbot.tool;

import com.nhnacademy.inventory.chatbot.dto.response.ReorderSuggestionResponse;
import com.nhnacademy.inventory.chatbot.service.ChatbotStorageAccessService;
import com.nhnacademy.inventory.chatbot.service.ReorderSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReorderSuggestionTool {
    private final ReorderSuggestionService reorderSuggestionService;
    private final ChatbotStorageAccessService chatbotStorageAccessService;

    @Tool(
            name = "getReorderSuggestion",
            description = """
                    최근 출고 이력을 분석해 다음 주에 발주가 필요한 의약품과 권장 수량을 계산합니다.
                    
                            사용 예시
                            - 다음주에 뭐 발주해야 해?
                            - 발주 추천해줘 / 입고 추천해줘
                            - 출고량 기준으로 부족할 재고 알려줘
                    
                            분석 주 수는 선택사항입니다. (미지정 시 최근 4주 기준)
                    """)
    public ReorderSuggestionResponse getReorderSuggestion(
            @ToolParam(description = "분석 주 수", required = false) Integer analysisWeeks
    ) {
        List<Long> storageIds = chatbotStorageAccessService.getAccessibleStorageIds();
        return reorderSuggestionService.analyze(storageIds, analysisWeeks);

    }

}
