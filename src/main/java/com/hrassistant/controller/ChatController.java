package com.hrassistant.controller;

import com.hrassistant.model.ChatRequest;
import com.hrassistant.model.ChatResponse;
import com.hrassistant.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;

    /**
     * Handles chat requests using RAG pipeline.
     *
     * @param request Chat request containing the user's question
     * @return Chat response with answer and sources
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("Received chat request: {}", request.getQuestion());

        ChatResponse response = ragService.chat(request);

        log.info("Returning response with {} sources", response.getSources().size());
        return ResponseEntity.ok(response);
    }
}
