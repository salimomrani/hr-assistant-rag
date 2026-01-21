package com.hrassistant.service;

import com.hrassistant.exception.HrAssistantException;
import com.hrassistant.model.ChatRequest;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for streaming RAG responses.
 *
 * Uses the same RAG pipeline as RagService but streams tokens in real-time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingRagService {

    private final GuardrailService guardrailService;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final StreamingChatModel streamingChatModel;

    @Value("classpath:prompts/rag-prompt.txt")
    private Resource promptTemplate;

    /**
     * Processes a chat request using RAG pipeline with streaming response.
     *
     * Pipeline:
     * 1. Validate question
     * 2. Embed question
     * 3. Search similar chunks
     * 4. Build context
     * 5. Stream response token by token
     * 6. Add sources at end
     *
     * @param request The chat request
     * @return Flux of response tokens
     */
    public Flux<String> chatStream(ChatRequest request) {
        String question = request.getQuestion();
        log.info("Processing streaming question: {}", question);

        try {
            // Step 1: Validate question
            guardrailService.validateQuestion(question);

            // Step 2: Embed question
            Embedding questionEmbedding = embeddingService.embed(question);

            // Step 3: Search similar chunks
            List<EmbeddingMatch<TextSegment>> matches = vectorStoreService.search(questionEmbedding);

            // Check if relevant information was found
            if (matches.isEmpty()) {
                log.warn("No relevant documents found for question: {}", question);
                return Flux.just("Je n'ai pas trouvé d'information pertinente dans les documents disponibles " +
                        "pour répondre à votre question. Je vous suggère de contacter directement le service RH " +
                        "pour obtenir une réponse précise.");
            }

            // Step 4: Build context from retrieved chunks
            String context = buildContext(matches);

            // Step 5: Build prompt
            String prompt = buildPrompt(context, question);

            // Step 6: Extract sources for later
            List<String> sources = extractSources(matches);

            // Step 7: Stream response
            return streamResponse(prompt, sources);

        } catch (HrAssistantException e) {
            log.error("RAG pipeline error: {}", e.getMessage());
            return Flux.error(e);
        } catch (Exception e) {
            log.error("Unexpected error during streaming: {}", e.getMessage(), e);
            return Flux.error(new HrAssistantException(
                    HrAssistantException.ErrorCode.INTERNAL_ERROR,
                    "Une erreur inattendue s'est produite",
                    e
            ));
        }
    }

    /**
     * Streams the LLM response token by token.
     */
    private Flux<String> streamResponse(String prompt, List<String> sources) {
        return Flux.create(sink -> streamingChatModel.chat(prompt, createStreamingHandler(sink, sources)));
    }

    /**
     * Creates a streaming response handler that feeds tokens to the Flux sink.
     */
    private StreamingChatResponseHandler createStreamingHandler(
            reactor.core.publisher.FluxSink<String> sink,
            List<String> sources) {
        return new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String token) {
                log.trace("Received token: {}", token);
                sink.next(token);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                log.info("Streaming complete. Adding sources: {}", sources);
                appendSources(sink, sources);
                sink.complete();
            }

            @Override
            public void onError(Throwable error) {
                log.error("Streaming error: {}", error.getMessage(), error);
                sink.error(wrapStreamingError(error));
            }
        };
    }

    /**
     * Appends source citations to the stream.
     * Adds extra spacing before sources for visual separation in frontend.
     */
    private void appendSources(reactor.core.publisher.FluxSink<String> sink, List<String> sources) {
        if (!sources.isEmpty()) {
            // Add extra newlines for visual spacing (frontend will render with pre-line)
            String sourcesText = "\n\n\n\n**Sources:**\n" +
                    sources.stream()
                            .map(source -> "- " + source)
                            .collect(Collectors.joining("\n"));
            sink.next(sourcesText);
        }
    }

    /**
     * Wraps streaming errors into HrAssistantException.
     */
    private HrAssistantException wrapStreamingError(Throwable error) {
        return new HrAssistantException(
                HrAssistantException.ErrorCode.LLM_ERROR,
                "Le service de génération de réponses est temporairement indisponible. " +
                        "Veuillez réessayer plus tard.",
                error
        );
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
     * Extracts unique document names from matches.
     */
    private List<String> extractSources(List<EmbeddingMatch<TextSegment>> matches) {
        return matches.stream()
                .map(match -> match.embedded().metadata().getString("documentName"))
                .distinct()
                .collect(Collectors.toList());
    }
}
