package com.nhnacademy.inventory.chatbot.tool;

import com.nhnacademy.inventory.assistant.dto.AssistantNoteResponse;
import com.nhnacademy.inventory.assistant.service.AssistantNoteQueryService;
import com.nhnacademy.inventory.chatbot.dto.response.AssistantNoteChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AssistantNoteTool {

    private static final DateTimeFormatter CREATED_AT_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private static final int MAX_NOTES = 5;

    private final AssistantNoteQueryService assistantNoteQueryService;

    @Tool(
            name = "getAssistantNotes",
            description = """
                입고와 출고 이후 시스템이 자동으로 점검해 남긴 알림을 조회합니다.
                현재 재고나 유통기한을 직접 조회할 때는 사용하지 마세요.

                사용 예시
                - 내가 놓친 거 있어?
                - 최근에 경고 뜬 거 알려줘
                - 아까 그 알림 다시 보여줘

                unreadOnly를 생략하면 읽은 것까지 최근 알림을 함께 조회합니다.
            """
    )
    public AssistantNoteChatResponse getAssistantNotes(
            @ToolParam(description = "안 읽은 알림만 조회할지 여부", required = false) Boolean unreadOnly
    ) {
        var result = assistantNoteQueryService.getNotes(Boolean.TRUE.equals(unreadOnly));

        List<AssistantNoteChatResponse.Note> notes = result.notes().stream()
                .limit(MAX_NOTES)
                .map(this::toNote)
                .toList();

        return new AssistantNoteChatResponse(result.unreadCount(), notes);
    }

    private AssistantNoteChatResponse.Note toNote(AssistantNoteResponse note) {
        return new AssistantNoteChatResponse.Note(
                note.severity().name(),
                note.message(),
                note.createdAt().format(CREATED_AT_FORMAT));
    }
}
