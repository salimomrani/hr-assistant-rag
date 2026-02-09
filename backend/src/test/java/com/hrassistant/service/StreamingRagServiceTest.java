package com.hrassistant.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hrassistant.exception.HrAssistantException;
import com.hrassistant.model.ChatRequest;
import com.hrassistant.model.OutputGuardrailResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class StreamingRagServiceTest {

  @Mock private GuardrailService guardrailService;
  @Mock private VectorStoreService vectorStoreService;
  @Mock private ChatModel chatModel;

  private StreamingRagService streamingRagService;

  @BeforeEach
  void setUp() {
    streamingRagService = new StreamingRagService(guardrailService, vectorStoreService, chatModel);

    // Set @Value fields via reflection
    ReflectionTestUtils.setField(
        streamingRagService,
        "systemPromptResource",
        new ByteArrayResource("You are an HR assistant.".getBytes()));
    ReflectionTestUtils.setField(
        streamingRagService,
        "userPromptTemplate",
        new ByteArrayResource("Documents:\n{{documents}}\n\nQuestion: {{question}}".getBytes()));
  }

  private Document createMockDocument(String text, String documentName) {
    return new Document(text, Map.of("documentName", documentName));
  }

  private void mockStreamResponse(String... tokens) {
    var responses =
        java.util.Arrays.stream(tokens)
            .map(
                token -> {
                  var message = new org.springframework.ai.chat.messages.AssistantMessage(token);
                  var gen = new Generation(message);
                  return new ChatResponse(List.of(gen));
                })
            .toList();

    when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.fromIterable(responses));
  }

  // ========================================================================
  // Happy Path
  // ========================================================================

  @Nested
  @DisplayName("Happy Path")
  class HappyPathTests {

    @Test
    @DisplayName("Streams response with sources appended")
    void streamsResponseWithSources() {
      ChatRequest request = ChatRequest.builder().question("How many vacation days?").build();

      doNothing().when(guardrailService).validateQuestion(anyString());
      when(vectorStoreService.search(anyString(), isNull()))
          .thenReturn(List.of(createMockDocument("You get 25 days.", "conges.pdf")));

      mockStreamResponse("You get ", "25 days.");

      when(guardrailService.validateOutput(anyString()))
          .thenReturn(new OutputGuardrailResult(true, List.of(), null));

      StepVerifier.create(streamingRagService.chatStream(request))
          .expectNext("You get ")
          .expectNext("25 days.")
          .expectNext("\n\n\n\n**Sources:**\n- conges.pdf")
          .verifyComplete();
    }

    @Test
    @DisplayName("Extracts distinct source names from multiple documents")
    void extractsDistinctSources() {
      ChatRequest request = ChatRequest.builder().question("Leave policy?").build();

      doNothing().when(guardrailService).validateQuestion(anyString());
      when(vectorStoreService.search(anyString(), isNull()))
          .thenReturn(
              List.of(
                  createMockDocument("Chunk1", "policy.pdf"),
                  createMockDocument("Chunk2", "policy.pdf"),
                  createMockDocument("Chunk3", "rules.pdf")));

      mockStreamResponse("Answer here.");

      when(guardrailService.validateOutput(anyString()))
          .thenReturn(new OutputGuardrailResult(true, List.of(), null));

      StepVerifier.create(streamingRagService.chatStream(request))
          .expectNext("Answer here.")
          .expectNext("\n\n\n\n**Sources:**\n- policy.pdf\n- rules.pdf")
          .verifyComplete();
    }
  }

  // ========================================================================
  // Edge Cases
  // ========================================================================

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {

    @Test
    @DisplayName("Returns fallback when no documents match")
    void returnsFallbackWhenNoDocumentsMatch() {
      ChatRequest request = ChatRequest.builder().question("Obscure question?").build();

      doNothing().when(guardrailService).validateQuestion(anyString());
      when(vectorStoreService.search(anyString(), isNull())).thenReturn(List.of());

      StepVerifier.create(streamingRagService.chatStream(request))
          .expectNextMatches(msg -> msg.contains("could not find relevant information"))
          .verifyComplete();
    }

    @Test
    @DisplayName("Passes documentIds filter to vectorStoreService")
    void passesDocumentIdsFilter() {
      List<String> docIds = List.of("doc-1", "doc-2");
      ChatRequest request =
          ChatRequest.builder().question("Filtered question?").documentIds(docIds).build();

      doNothing().when(guardrailService).validateQuestion(anyString());
      when(vectorStoreService.search(anyString(), any()))
          .thenReturn(List.of(createMockDocument("Content", "doc.pdf")));

      mockStreamResponse("Filtered answer.");

      when(guardrailService.validateOutput(anyString()))
          .thenReturn(new OutputGuardrailResult(true, List.of(), null));

      StepVerifier.create(streamingRagService.chatStream(request))
          .expectNext("Filtered answer.")
          .expectNext("\n\n\n\n**Sources:**\n- doc.pdf")
          .verifyComplete();

      verify(vectorStoreService).search("Filtered question?", docIds);
    }
  }

  // ========================================================================
  // Error Scenarios
  // ========================================================================

  @Nested
  @DisplayName("Error Scenarios")
  class ErrorTests {

    @Test
    @DisplayName("Emits error when guardrail rejects question")
    void emitsErrorWhenGuardrailRejects() {
      ChatRequest request = ChatRequest.builder().question("What is the weather?").build();

      doThrow(
              new HrAssistantException(
                  HrAssistantException.ErrorCode.INVALID_INPUT, "Off-topic question"))
          .when(guardrailService)
          .validateQuestion(anyString());

      StepVerifier.create(streamingRagService.chatStream(request))
          .expectError(HrAssistantException.class)
          .verify();
    }

    @Test
    @DisplayName("Returns fallback message when output guardrail flags unsafe content")
    void returnsFallbackWhenOutputUnsafe() {
      ChatRequest request = ChatRequest.builder().question("Salary info?").build();

      doNothing().when(guardrailService).validateQuestion(anyString());
      when(vectorStoreService.search(anyString(), isNull()))
          .thenReturn(List.of(createMockDocument("Salary is 3500 euros.", "salaries.pdf")));

      mockStreamResponse("The salary is 3500 euros.");

      when(guardrailService.validateOutput(anyString()))
          .thenReturn(new OutputGuardrailResult(false, List.of("SALARY detected"), null));

      StepVerifier.create(streamingRagService.chatStream(request))
          .expectNext("I am unable to answer this question. Please contact HR directly.")
          .verifyComplete();
    }
  }
}
