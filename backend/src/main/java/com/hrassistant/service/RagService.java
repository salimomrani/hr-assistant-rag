package com.hrassistant.service;

import com.hrassistant.model.ChatRequest;
import com.hrassistant.model.ChatResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

  private final StreamingRagService streamingRagService;

  /**
   * Processes a chat request using RAG pipeline.
   *
   * <p>Internally uses StreamingRagService and collects all tokens into a complete response. This
   * provides a blocking API while reusing the same RAG logic.
   *
   * <p>Pipeline: 1. Delegate to StreamingRagService 2. Collect all streaming tokens 3. Parse
   * sources from response 4. Return complete ChatResponse
   */
  public ChatResponse chat(ChatRequest request) {
    String question = request.getQuestion();
    log.info("Processing question: {}", question);

    try {
      // Use StreamingRagService and collect all tokens
      List<String> tokens = streamingRagService.chatStream(request).collectList().block();

      if (tokens == null || tokens.isEmpty()) {
        log.warn("No response received from streaming service");
        return buildNoResultsResponse(request);
      }

      // Join all tokens into complete answer
      String fullResponse = String.join("", tokens);

      // Extract sources from response (they are appended at the end)
      List<String> sources = SourceParsingUtil.extractSources(fullResponse);
      String answerWithoutSources = SourceParsingUtil.removeSourcesSection(fullResponse);

      log.info("Generated response with {} sources", sources.size());

      return ChatResponse.builder()
          .answer(answerWithoutSources)
          .sources(sources)
          .conversationId(generateConversationId(request))
          .build();

    } catch (Exception e) {
      log.error("Failed to generate response: {}", e.getMessage(), e);
      throw e;
    }
  }

  /** Builds response when no relevant documents are found. */
  private ChatResponse buildNoResultsResponse(ChatRequest request) {
    return ChatResponse.builder()
        .answer(
            "I could not find relevant information in the available documents to answer your"
                + " question. I suggest contacting the HR department directly for a precise"
                + " answer.")
        .sources(List.of())
        .conversationId(generateConversationId(request))
        .build();
  }

  /** Generates or retrieves conversation ID. */
  private String generateConversationId(ChatRequest request) {
    if (StringUtils.hasText(request.getConversationId())) {
      return request.getConversationId();
    }
    return UUID.randomUUID().toString();
  }
}
