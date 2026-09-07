package com.nhnacademy.inventory.chatbot.service;

import com.nhnacademy.inventory.chatbot.dto.response.ChatResponse;
import com.nhnacademy.inventory.chatbot.tool.ExpiringInventoryTool;
import com.nhnacademy.inventory.chatbot.tool.LowStockInventoryTool;
import com.nhnacademy.inventory.chatbot.tool.MedicineInboundTool;
import com.nhnacademy.inventory.chatbot.tool.MedicineInventorySearchTool;
import com.nhnacademy.inventory.chatbot.tool.MedicineOutboundTool;
import com.nhnacademy.inventory.global.util.UserContext;
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

        registerMedicineInbound와 registerMedicineOutbound는 실제 재고를 변경합니다.
        필수 정보가 빠졌으면 Tool을 호출하지 말고 사용자에게 필요한 정보를 질문하세요.
        모든 처리 정보를 먼저 요약하고 사용자가 명시적으로 최종 확인한 뒤에만 Tool을 한 번 호출하세요.
        Tool 결과의 success가 false이면 재고가 변경되지 않은 것으로 안내하고 message에 따라 다시 질문하세요.
        message에 medicinePackageUnitId 또는 zoneId가 포함된 후보가 있으면 후보 식별 ID를 함께 안내하세요.
        사용자가 후보를 선택하면 해당 ID를 다음 registerMedicineInbound 호출에 포함하세요.
        success가 true이면 Tool 결과에 포함된 처리 내역만 안내하세요.
        """;

    private final ChatClient chatClient;

    public ChatbotService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            MedicineInventorySearchTool medicineInventorySearchTool,
            ExpiringInventoryTool expiringInventoryTool,
            LowStockInventoryTool lowStockInventoryTool,
            MedicineInboundTool medicineInboundTool,
            MedicineOutboundTool medicineOutboundTool
    ) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(
                        medicineInventorySearchTool,
                        expiringInventoryTool,
                        lowStockInventoryTool,
                        medicineInboundTool,
                        medicineOutboundTool
                )
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
