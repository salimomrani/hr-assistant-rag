package com.hrassistant.service;

import com.hrassistant.exception.HrAssistantException;
import com.hrassistant.model.ChatRequest;
import com.hrassistant.model.ChatResponse;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final GuardrailService guardrailService;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final ChatModel chatModel;

    @Value("classpath:prompts/rag-prompt.txt")
    private Resource promptTemplate;

    /**
     * Processes a chat request using RAG pipeline.
     *
     * Pipeline:
     * 1. Validate question
     * 2. Embed question
     * 3. Search similar chunks
     * 4. Build context
     * 5. Generate response
     * 6. Extract sources
     */
    public ChatResponse chat(ChatRequest request) {
        String question = request.getQuestion();
        log.info("Processing question: {}", question);

        // Step 1: Validate question
        guardrailService.validateQuestion(question);

        // Step 2: Embed question
        Embedding questionEmbedding = embeddingService.embed(question);

        // Step 3: Search similar chunks
        List<EmbeddingMatch<TextSegment>> matches = vectorStoreService.search(questionEmbedding);

        // Check if relevant information was found
        if (matches.isEmpty()) {
            log.warn("No relevant documents found for question: {}", question);
            return buildNoResultsResponse(request);
        }

        // Step 4: Build context from retrieved chunks
        String context = buildContext(matches);

        // Step 5: Generate response using LLM
        String prompt = buildPrompt(context, question);
        String answer = generateAnswer(prompt);

        // Step 6: Extract sources
        List<String> sources = extractSources(matches);

        log.info("Generated response with {} sources", sources.size());

        return ChatResponse.builder()
                .answer(answer)
                .sources(sources)
                .conversationId(generateConversationId(request))
                .build();
    }

    /**
     * Builds context from retrieved document chunks.
     */
    private String buildContext(List<EmbeddingMatch<TextSegment>> matches) {
        return matches.stream()
                .map(match -> {
                    TextSegment segment = match.embedded();
                    String docName = segment.metadata().getString("documentName");
                    String text = segment.text();
                    return String.format("[Source: %s]\n%s", docName, text);
                })
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * Builds the final prompt by replacing template variables.
     */
    private String buildPrompt(String context, String question) {
        try {
            String template = promptTemplate.getContentAsString(StandardCharsets.UTF_8);
            return template
                    .replace("{{documents}}", context)
                    .replace("{{question}}", question);
        } catch (IOException e) {
            log.error("Failed to load prompt template", e);
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.INTERNAL_ERROR,
                    "Failed to load prompt template",
                    e
            );
        }
    }

    /**
     * Generates answer using the LLM.
     */
    private String generateAnswer(String prompt) {
        try {
            log.debug("Generating answer with LLM");
            return chatModel.chat(prompt);
        } catch (Exception e) {
            log.error("LLM generation failed: {}", e.getMessage(), e);
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.LLM_ERROR,
                    "Le service de génération de réponses est temporairement indisponible. Veuillez réessayer plus tard.",
                    e
            );
        }
    }

    /**
     * Extracts unique document names from matches.
     */
    private List<String> extractSources(List<EmbeddingMatch<TextSegment>> matches) {
        return matches.stream()
                .map(match -> match.embedded().metadata().getString("documentName"))
                .distinct()
                .collect(Collectors.toList());
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
