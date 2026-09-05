package com.nhnacademy.inventory.telegram.service;

import com.nhnacademy.inventory.telegram.tool.TelegramExpiringInventoryTool;
import com.nhnacademy.inventory.telegram.tool.TelegramLowStockInventoryTool;
import com.nhnacademy.inventory.telegram.tool.TelegramMedicineInventorySearchTool;
import com.nhnacademy.inventory.telegram.tool.TelegramReorderSuggestionTool;
import com.nhnacademy.inventory.telegram.tool.TelegramToolContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TelegramChatbotService {

    private static final String SYSTEM_PROMPT = """
            당신은 의약품 재고 관리 챗봇입니다.
            지금은 부서 단톡방에서 여러 사람이 함께 보는 중입니다.

            질문에 맞는 Tool을 사용해 조회하고, 결과에 없는 정보는 추측하지 마세요.
            답변은 간결한 한국어 일반 텍스트로 작성하고 Markdown 문법은 사용하지 마세요.
            단톡방이므로 답변은 짧게, 핵심만 전하세요.

            이전 대화 내용이 있으면 이어서 답하세요.
            다만 이전 답변의 수치를 그대로 재사용하지 말고 필요한 Tool을 다시 호출해 확인하세요.

            searchMedicineInventory 결과:
            의약품명, 수량, 저장소와 구역을 안내하세요. 같은 의약품이 여러 위치에 있으면 합계도 함께 알려주세요.

            getExpiringInventory 결과:
            의약품명, 유통기한, 수량, 위치를 안내하고 유통기한이 이른 것부터 알려주세요.
            사용자가 90일을 넘는 기간을 요청했다면 최대 90일 기준으로 조회했다고 먼저 알리세요.

            getLowStockInventory 결과:
            의약품명, 현재 수량, 최소재고 기준, 위치를 안내하세요.
            수량이 0이면 품절임을 알리세요.

            getReorderSuggestion 결과:
            analyzedWeeks 기준으로 분석했다고 먼저 알리고, 의약품명, 위치, 권장 발주 수량, 현재 수량을 안내하세요.
            daysUntilStockout이 있으면 예상 소진 일수가 작은 것부터 알리세요.
            items가 비어 있으면 다음 주에 발주가 필요한 의약품이 없다고 답하세요.

            조회 결과가 비어 있으면 해당하는 재고가 없다고 답하세요.
            조회 결과는 최대 10건까지만 제공됩니다.
            """;

    private final ChatClient chatClient;
    private final TelegramStorageAccessService storageAccessService;

    public TelegramChatbotService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            TelegramMedicineInventorySearchTool medicineInventorySearchTool,
            TelegramExpiringInventoryTool expiringInventoryTool,
            TelegramLowStockInventoryTool lowStockInventoryTool,
            TelegramReorderSuggestionTool reorderSuggestionTool,
            TelegramStorageAccessService storageAccessService
    ) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(
                        medicineInventorySearchTool,
                        expiringInventoryTool,
                        lowStockInventoryTool,
                        reorderSuggestionTool
                )
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.storageAccessService = storageAccessService;
    }

    public String answer(Long departmentId, String conversationId, String question) {
        List<Long> storageIds = storageAccessService.getAccessibleStorageIds(departmentId);

        if (storageIds.isEmpty()) {
            return "이 부서에 연결된 저장소가 없어 재고를 조회할 수 없습니다.";
        }

        try {
            return chatClient
                    .prompt()
                    .user(question)
                    // 조회 범위는 요청마다 ToolContext로 넘긴다. LLM에는 보이지 않는다.
                    .toolContext(TelegramToolContext.of(storageIds))
                    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[TelegramChatbot] AI 응답 생성 실패. departmentId={}", departmentId, e);
            return "챗봇 응답을 생성하는 중 오류가 발생했습니다. 다시 시도해주세요.";
        }
    }
}
