package com.nhnacademy.inventory.chatbot.controller;

import com.nhnacademy.inventory.chatbot.dto.response.ChatResponse;
import com.nhnacademy.inventory.chatbot.service.ChatbotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatbotController.class)
@DisplayName("챗봇 Controller 테스트")
class ChatbotControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatbotService chatbotService;

    @Test
    @DisplayName("질문을 챗봇 서비스에 전달하고 답변을 반환한다")
    void chat() throws Exception {
        given(chatbotService.chat(anyString())).willReturn(new ChatResponse("재고가 없습니다."));

        mockMvc.perform(post("/api/core/chatbot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"타이레놀 재고 알려줘\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"success":true,"data":{"message":"재고가 없습니다."}}
                        """));
    }

    @Test
    @DisplayName("질문이 비어 있으면 validation 오류를 반환한다")
    void chatWithBlankMessage() throws Exception {
        mockMvc.perform(post("/api/core/chatbot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\" \"}"))
                .andExpect(status().is4xxClientError());
    }
}
