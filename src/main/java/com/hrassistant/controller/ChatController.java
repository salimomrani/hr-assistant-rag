package com.hrassistant.controller;

import com.hrassistant.model.ChatRequest;
import com.hrassistant.model.ChatResponse;
import com.hrassistant.service.RagService;
import com.hrassistant.service.StreamingRagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;
    private final StreamingRagService streamingRagService;

    /**
     * Handles chat requests using RAG pipeline.
     * Returns complete response in one blocking call.
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

    /**
     * Handles streaming chat requests using RAG pipeline.
     * Returns tokens progressively via Server-Sent Events (SSE).
     *
     * Example usage:
     * POST /api/chat/stream
     * Content-Type: application/json
     * Body: {"question": "Combien de jours de congés?"}
     *
     * @param request Chat request containing the user's question
     * @return Flux of response tokens streamed in real-time
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {

        log.info("Received streaming chat request: {}", request.getQuestion());

        return streamingRagService.chatStream(request)
                .doOnComplete(() -> log.info("Streaming completed for question: {}", request.getQuestion()))
                .doOnError(error -> log.error("Streaming error for question: {}", request.getQuestion(), error));
    }
}
