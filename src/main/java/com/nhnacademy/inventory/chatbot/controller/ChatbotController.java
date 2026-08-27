package com.nhnacademy.inventory.chatbot.controller;

import com.nhnacademy.inventory.chatbot.dto.request.ChatRequest;
import com.nhnacademy.inventory.chatbot.dto.response.ChatResponse;
import com.nhnacademy.inventory.chatbot.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core/chatbot")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatbotService.chat(request.message());
    }
}
