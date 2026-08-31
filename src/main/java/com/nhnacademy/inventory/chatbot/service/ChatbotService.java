package com.nhnacademy.inventory.chatbot.service;

import com.nhnacademy.inventory.chatbot.dto.response.ChatResponse;
import com.nhnacademy.inventory.chatbot.tool.ExpiringInventoryTool;
import com.nhnacademy.inventory.chatbot.tool.LowStockInventoryTool;
import com.nhnacademy.inventory.chatbot.tool.MedicineInventorySearchTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatbotService {
    private static final String SYSTEM_PROMPT = """
        당신은 의약품 재고 관리 챗봇입니다.
        질문에 맞는 Tool을 사용해 조회하고, 결과에 없는 정보는 추측하지 마세요.
        답변은 간결한 한국어 일반 텍스트로 작성하고 Markdown 문법은 사용하지 마세요.

        searchMedicineInventory 결과:
        의약품명, 총 수량, 위치별 수량을 안내하세요.

        getExpiringInventory 결과:
        searchDays 기준으로 유통기한 임박 의약품명, 유통기한, 남은 일수, 수량, 위치를 안내하세요.
        사용자가 요청한 기간이 90일을 초과해 searchDays가 90이면,
        최대 90일 기준으로 조회했다고 먼저 알리세요.

        getLowStockInventory 결과:
        의약품명, 현재 수량, 최소재고 기준, 위치를 안내하세요.
        수량이 0이면 품절임을 안내하세요.
        """;

    private final ChatClient chatClient;

    public ChatbotService(
            ChatClient.Builder chatClientBuilder,
            MedicineInventorySearchTool medicineInventorySearchTool,
            ExpiringInventoryTool expiringInventoryTool,
            LowStockInventoryTool lowStockInventoryTool
    ) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(medicineInventorySearchTool, expiringInventoryTool, lowStockInventoryTool)
                .build();
    }

    public ChatResponse chat(String message) {
        try {
            String answer = chatClient
                    .prompt()
                    .user(message)
                    .call()
                    .content();

            return new ChatResponse(answer);
        } catch (Exception e) {
            log.error("[Chatbot] AI 응답 생성 실패", e);
            return new ChatResponse("챗봇 응답을 생성하는 중 오류가 발생했습니다. \n 다시 시도해주세요.");
        }
    }
}
