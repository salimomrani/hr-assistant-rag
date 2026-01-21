package com.hrassistant.service;

import com.hrassistant.model.ChatRequest;
import com.hrassistant.model.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final StreamingRagService streamingRagService;

    /**
     * Processes a chat request using RAG pipeline.
     *
     * Internally uses StreamingRagService and collects all tokens into a complete response.
     * This provides a blocking API while reusing the same RAG logic.
     *
     * Pipeline:
     * 1. Delegate to StreamingRagService
     * 2. Collect all streaming tokens
     * 3. Parse sources from response
     * 4. Return complete ChatResponse
     */
    public ChatResponse chat(ChatRequest request) {
        String question = request.getQuestion();
        log.info("Processing question: {}", question);

        try {
            // Use StreamingRagService and collect all tokens
            List<String> tokens = streamingRagService.chatStream(request)
                    .collectList()
                    .block();

            if (tokens == null || tokens.isEmpty()) {
                log.warn("No response received from streaming service");
                return buildNoResultsResponse(request);
            }

            // Join all tokens into complete answer
            String fullResponse = String.join("", tokens);

            // Extract sources from response (they are appended at the end)
            List<String> sources = extractSourcesFromResponse(fullResponse);
            String answerWithoutSources = removeSourcesFromResponse(fullResponse);

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

    /**
     * Extracts sources from the response text.
     * Sources are appended by StreamingRagService in format: "**Sources:**\n- source1\n- source2"
     */
    private List<String> extractSourcesFromResponse(String response) {
        if (!response.contains("**Sources:**")) {
            return List.of();
        }

        String sourcesSection = response.substring(response.indexOf("**Sources:**"));
        return sourcesSection.lines()
                .filter(line -> line.trim().startsWith("- "))
                .map(line -> line.trim().substring(2))
                .collect(Collectors.toList());
    }

    /**
     * Removes the sources section from the response text.
     */
    private String removeSourcesFromResponse(String response) {
        if (!response.contains("**Sources:**")) {
            return response;
        }
        return response.substring(0, response.indexOf("**Sources:**")).trim();
    }

    /**
     * Builds response when no relevant documents are found.
     */
    private ChatResponse buildNoResultsResponse(ChatRequest request) {
        return ChatResponse.builder()
                .answer("Je n'ai pas trouvé d'information pertinente dans les documents disponibles pour répondre à votre question. " +
                        "Je vous suggère de contacter directement le service RH pour obtenir une réponse précise.")
                .sources(List.of())
                .conversationId(generateConversationId(request))
                .build();
    }

    /**
     * Generates or retrieves conversation ID.
     */
    private String generateConversationId(ChatRequest request) {
        if (StringUtils.hasText(request.getConversationId())) {
            return request.getConversationId();
        }
        return UUID.randomUUID().toString();
    }
}
