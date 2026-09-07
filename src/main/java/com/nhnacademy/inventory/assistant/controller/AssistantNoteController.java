package com.nhnacademy.inventory.assistant.controller;

import com.nhnacademy.inventory.assistant.dto.AssistantNoteListResponse;
import com.nhnacademy.inventory.assistant.service.AssistantNoteQueryService;
import com.nhnacademy.inventory.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core/assistant/notes")
@RequiredArgsConstructor
public class AssistantNoteController {

    private final AssistantNoteQueryService assistantNoteQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<AssistantNoteListResponse>> getNotes(
            @RequestParam(name = "unread", defaultValue = "false") boolean unreadOnly
    ) {
        return ResponseEntity.ok(ApiResponse.success(assistantNoteQueryService.getNotes(unreadOnly)));
    }

    @PostMapping("/{note-id}/read")
    public ResponseEntity<Void> markRead(@PathVariable("note-id") Long noteId) {
        assistantNoteQueryService.markRead(noteId);

        return ResponseEntity.noContent().build();
    }
}
