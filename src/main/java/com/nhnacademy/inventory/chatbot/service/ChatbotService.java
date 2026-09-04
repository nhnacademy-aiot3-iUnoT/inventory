package com.nhnacademy.inventory.chatbot.service;

import com.nhnacademy.inventory.chatbot.dto.response.ChatResponse;
import com.nhnacademy.inventory.chatbot.tool.ReorderSuggestionTool;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.chatbot.tool.ExpiringInventoryTool;
import com.nhnacademy.inventory.chatbot.tool.LowStockInventoryTool;
import com.nhnacademy.inventory.chatbot.tool.MedicineInventorySearchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class ChatbotService {
    private static final String SYSTEM_PROMPT = """
            당신은 의약품 재고 관리 챗봇입니다.
            질문에 맞는 Tool을 사용해 조회하고, 결과에 없는 정보는 추측하지 마세요.
            답변은 간결한 한국어 일반 텍스트로 작성하고 Markdown 문법은 사용하지 마세요.
            
            이전 대화 내용이 있으면 이어서 답하세요.
            사용자가 "그 중에", "방금 것" 처럼 앞선 답변을 가리키면 그 맥락으로 이해하세요.
            다만 이전 답변의 수치를 그대로 재사용하지 말고 필요한 Tool을 다시 호출해 확인하세요.
            
            searchMedicineInventory 결과:
            의약품명, 총 수량, 위치별 수량을 안내하세요.
            
            getExpiringInventory 결과:
            searchDays 기준으로 유통기한 임박 의약품명, 유통기한, 남은 일수, 수량, 위치를 안내하세요.
            사용자가 요청한 기간이 90일을 초과해 searchDays가 90이면,
            최대 90일 기준으로 조회했다고 먼저 알리세요.
            
            getLowStockInventory 결과:
            의약품명, 현재 수량, 최소재고 기준, 위치를 안내하세요.
            수량이 0이면 품절임을 안내하세요.
            
            getReorderSuggestion 결과:
            analyzedWeeks 기준으로 분석했다고 먼저 알리고,
            의약품명, 포장단위, 위치, 권장 발주 수량, 현재 수량, 주간 평균 출고량을 안내하세요.
            daysUntilStockout이 있으면 예상 소진 일수를 함께 알리고, 값이 작은 것부터 안내하세요.
            expiringSoonQuantity가 0보다 크면 유통기한 임박 수량이 가용 재고에서 제외됐음을 알리세요.
            threshold가 null이면 최소재고 미설정으로 안내하세요.
            adjustmentApplied가 true이면 폐기율(disposalRate)을 반영해 수량을 줄였다고 알리세요.
            items가 비어 있으면 다음 주에 발주가 필요한 의약품이 없다고 안내하세요.
            결과는 최대 10건까지만 제공됩니다.
            """;

    private final ChatClient chatClient;

    public ChatbotService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            MedicineInventorySearchTool medicineInventorySearchTool,
            ExpiringInventoryTool expiringInventoryTool,
            LowStockInventoryTool lowStockInventoryTool,
            ReorderSuggestionTool reorderSuggestionTool
    ) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(medicineInventorySearchTool, expiringInventoryTool, lowStockInventoryTool, reorderSuggestionTool)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public ChatResponse chat(String message) {
        try {
            String answer = chatClient
                    .prompt()
                    .user(message)
                    .advisors(advisor ->
                            advisor.param(ChatMemory.CONVERSATION_ID, resolveConversationId()))
                    .call()
                    .content();

            return new ChatResponse(answer);
        } catch (Exception e) {
            log.error("[Chatbot] AI 응답 생성 실패", e);
            return new ChatResponse("챗봇 응답을 생성하는 중 오류가 발생했습니다. \n 다시 시도해주세요.");
        }
    }

    private String resolveConversationId() {
        UUID userUuid = UserContext.getUserUuid();

        // 사용자 UUID를 대화 번호로 설정 (사용자를 알 수 없으면 랜덤 번호 생성)
        return (userUuid != null) ? userUuid.toString() : UUID.randomUUID().toString();
    }
}
