package com.hrassistant.service;

import com.hrassistant.exception.HrAssistantException;
import com.hrassistant.model.ChatRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture; // NOSONAR - used with var inference
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/** Service for streaming RAG responses using Spring AI. */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingRagService {

  private final GuardrailService guardrailService;
  private final VectorStoreService vectorStoreService;
  private final ChatModel chatModel;

  @Value("classpath:prompts/rag-system-prompt.txt")
  private Resource systemPromptResource;

  @Value("classpath:prompts/rag-prompt.txt")
  private Resource userPromptTemplate;

  /**
   * Processes a chat request using RAG pipeline with streaming response.
   *
   * <p>Pipeline: 1. Validate question 2. Search similar chunks (embedding done internally by
   * VectorStore) 3. Build context 4. Stream response token by token 5. Add sources at end
   */
  public Flux<String> chatStream(ChatRequest request) {
    String question = request.getQuestion();
    List<String> documentIds = request.getDocumentIds();
    log.info(
        "Processing streaming question: '{}' with documentIds filter: {}", question, documentIds);

    try {
      // Run guardrail validation and vector search in parallel
      var guardrailFuture =
          CompletableFuture.runAsync(() -> guardrailService.validateQuestion(question));
      var searchFuture =
          CompletableFuture.supplyAsync(() -> vectorStoreService.search(question, documentIds));

      // Wait for both — guardrail may throw HrAssistantException
      guardrailFuture.join();
      List<Document> matches = searchFuture.join();

      // Check if relevant information was found
      if (matches.isEmpty()) {
        log.warn("No relevant documents found for question: {}", question);
        return Flux.just(
            "I could not find relevant information in the available documents to answer your"
                + " question. I suggest contacting the HR department directly for a precise"
                + " answer.");
      }

      // Build context, prompt and stream
      String context = buildContext(matches);
      Prompt prompt = buildPrompt(context, question);
      List<String> sources = extractSources(matches);
      return streamResponse(prompt, sources);

    } catch (java.util.concurrent.CompletionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof HrAssistantException hae) {
        log.error("RAG pipeline error: {}", hae.getMessage());
        return Flux.error(hae);
      }
      log.error("Unexpected error during streaming: {}", cause.getMessage(), cause);
      return Flux.error(
          new HrAssistantException(
              HrAssistantException.ErrorCode.INTERNAL_ERROR,
              "An unexpected error occurred",
              cause));
    } catch (HrAssistantException e) {
      log.error("RAG pipeline error: {}", e.getMessage());
      return Flux.error(e);
    } catch (Exception e) {
      log.error("Unexpected error during streaming: {}", e.getMessage(), e);
      return Flux.error(
          new HrAssistantException(
              HrAssistantException.ErrorCode.INTERNAL_ERROR, "An unexpected error occurred", e));
    }
  }

  private static final String OUTPUT_FALLBACK_MESSAGE =
      "I am unable to answer this question. Please contact HR directly.";

  /** Streams the LLM response token by token, validates the full output, then emits. */
  private Flux<String> streamResponse(Prompt prompt, List<String> sources) {
    return chatModel.stream(prompt)
        .map(
            response -> {
              String content = response.getResult().getOutput().getText();
              return content != null ? content : "";
            })
        .filter(content -> !content.isEmpty())
        .collectList()
        .flatMapMany(
            tokens -> {
              String fullResponse = String.join("", tokens);
              var guardrailResult = guardrailService.validateOutput(fullResponse);
              if (!guardrailResult.safe()) {
                return Flux.just(OUTPUT_FALLBACK_MESSAGE);
              }
              log.info("Streaming complete. Adding sources: {}", sources);
              return Flux.fromIterable(tokens)
                  .concatWith(Flux.just(SourceParsingUtil.buildSourcesText(sources)));
            })
        .doOnError(error -> log.error("Streaming error: {}", error.getMessage(), error))
        .onErrorMap(
            error ->
                new HrAssistantException(
                    HrAssistantException.ErrorCode.LLM_ERROR,
                    "The response generation service is temporarily unavailable. Please try again"
                        + " later.",
                    error));
  }

  /** Builds context from retrieved documents. */
  private String buildContext(List<Document> matches) {
    return String.join("\n\n---\n\n", matches.stream().map(Document::getText).toList());
  }

  /**
   * Builds the prompt with system message (instructions) and user message (documents + question).
   */
  private Prompt buildPrompt(String context, String question) {
    try {
      String systemText = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
      String userTemplate = userPromptTemplate.getContentAsString(StandardCharsets.UTF_8);
      String userText =
          userTemplate.replace("{{documents}}", context).replace("{{question}}", question);

      return new Prompt(List.of(new SystemMessage(systemText), new UserMessage(userText)));
    } catch (IOException e) {
      log.error("Failed to load prompt template", e);
      throw new HrAssistantException(
          HrAssistantException.ErrorCode.INTERNAL_ERROR, "Failed to load prompt template", e);
    }
  }

  /** Extracts unique document names from matches. */
  private List<String> extractSources(List<Document> matches) {
    return matches.stream()
        .map(doc -> Objects.toString(doc.getMetadata().get("documentName"), null))
        .filter(Objects::nonNull)
        .distinct()
        .toList();
  }
}
