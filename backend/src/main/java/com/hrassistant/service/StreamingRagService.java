package com.hrassistant.service;

import com.hrassistant.exception.HrAssistantException;
import com.hrassistant.model.ChatRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Value("classpath:prompts/rag-prompt.txt")
  private Resource promptTemplate;

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
      // Step 1: Validate question
      guardrailService.validateQuestion(question);

      // Step 2: Search similar chunks (with optional document filter)
      List<Document> matches = vectorStoreService.search(question, documentIds);

      // Check if relevant information was found
      if (matches.isEmpty()) {
        log.warn("No relevant documents found for question: {}", question);
        return Flux.just(
            "I could not find relevant information in the available documents to answer your"
                + " question. I suggest contacting the HR department directly for a precise"
                + " answer.");
      }

      // Step 3: Build context from retrieved chunks
      String context = buildContext(matches);

      // Step 4: Build prompt
      String promptText = buildPrompt(context, question);

      // Step 5: Extract sources for later
      List<String> sources = extractSources(matches);

      // Step 6: Stream response using Spring AI ChatModel
      return streamResponse(promptText, sources);

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
      "Je ne suis pas en mesure de répondre à cette question. "
          + "Veuillez contacter le service RH directement.";

  /** Streams the LLM response token by token, validates the full output, then emits. */
  private Flux<String> streamResponse(String promptText, List<String> sources) {
    Prompt prompt = new Prompt(promptText);

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
              return Flux.fromIterable(tokens).concatWith(Flux.just(buildSourcesText(sources)));
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

  /** Builds the sources text to append at the end of the response. */
  private String buildSourcesText(List<String> sources) {
    if (sources.isEmpty()) {
      return "";
    }
    return "\n\n\n\n**Sources:**\n"
        + String.join("\n", sources.stream().map(source -> "- " + source).toList());
  }

  /** Builds context from retrieved documents. */
  private String buildContext(List<Document> matches) {
    return String.join(
        "\n\n",
        matches.stream()
            .map(
                doc -> {
                  String docName = (String) doc.getMetadata().get("documentName");
                  String text = doc.getText();
                  return String.format("[Source: %s]\n%s", docName, text);
                })
            .toList());
  }

  /** Builds the final prompt by replacing template variables. */
  private String buildPrompt(String context, String question) {
    try {
      String template = promptTemplate.getContentAsString(StandardCharsets.UTF_8);
      return template.replace("{{documents}}", context).replace("{{question}}", question);
    } catch (IOException e) {
      log.error("Failed to load prompt template", e);
      throw new HrAssistantException(
          HrAssistantException.ErrorCode.INTERNAL_ERROR, "Failed to load prompt template", e);
    }
  }

  /** Extracts unique document names from matches. */
  private List<String> extractSources(List<Document> matches) {
    return matches.stream()
        .map(doc -> (String) doc.getMetadata().get("documentName"))
        .distinct()
        .toList();
  }
}
