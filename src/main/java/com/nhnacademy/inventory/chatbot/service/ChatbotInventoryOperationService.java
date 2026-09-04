package com.nhnacademy.inventory.chatbot.service;

import com.nhnacademy.inventory.chatbot.repository.MedicineInventoryChatbotRepository;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.service.InboundService;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.service.MedicineOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotInventoryOperationService {
    private final MedicineInventoryChatbotRepository inventoryRepository;
    private final ChatbotStorageAccessService accessService;
    private final InboundService inboundService;
    private final MedicineOutboundService outboundService;
}
