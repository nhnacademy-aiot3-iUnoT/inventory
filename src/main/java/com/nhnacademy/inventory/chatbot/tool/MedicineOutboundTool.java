package com.nhnacademy.inventory.chatbot.tool;

import com.nhnacademy.inventory.chatbot.service.ChatbotInventoryOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MedicineOutboundTool {
    private final ChatbotInventoryOperationService operationService;
}
