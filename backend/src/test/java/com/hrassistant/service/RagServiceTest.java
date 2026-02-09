package com.hrassistant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hrassistant.model.ChatRequest;
import com.hrassistant.model.ChatResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

  @Mock private StreamingRagService streamingRagService;

  @InjectMocks private RagService ragService;

  // ========================================================================
  // Happy Path
  // ========================================================================

  @Nested
  @DisplayName("Happy Path")
  class HappyPathTests {

    @Test
    @DisplayName("Returns response with sources extracted from streaming tokens")
    void returnsResponseWithSources() {
      ChatRequest request =
          ChatRequest.builder().question("How many vacation days do I have?").build();

      String responseWithSources =
          "You have 25 vacation days per year.\n\n\n\n**Sources:**\n- conges.pdf\n- reglement.pdf";
      when(streamingRagService.chatStream(any(ChatRequest.class)))
          .thenReturn(
              Flux.just(
                  "You have 25 vacation days per year.",
                  "\n\n\n\n**Sources:**\n- conges.pdf\n- reglement.pdf"));

      ChatResponse response = ragService.chat(request);

      assertThat(response.getAnswer()).isEqualTo("You have 25 vacation days per year.");
      assertThat(response.getSources()).containsExactly("conges.pdf", "reglement.pdf");
      assertThat(response.getConversationId()).isNotBlank();
    }

    @Test
    @DisplayName("Uses existing conversationId when provided")
    void usesExistingConversationId() {
      ChatRequest request =
          ChatRequest.builder()
              .question("What is the leave policy?")
              .conversationId("existing-id-123")
              .build();

      when(streamingRagService.chatStream(any(ChatRequest.class)))
          .thenReturn(Flux.just("The leave policy is..."));

      ChatResponse response = ragService.chat(request);

      assertThat(response.getConversationId()).isEqualTo("existing-id-123");
    }

    @Test
    @DisplayName("Generates new UUID conversationId when none provided")
    void generatesNewConversationId() {
      ChatRequest request = ChatRequest.builder().question("Test question").build();

      when(streamingRagService.chatStream(any(ChatRequest.class)))
          .thenReturn(Flux.just("Response"));

      ChatResponse response = ragService.chat(request);

      assertThat(response.getConversationId()).isNotBlank();
      // Should be a valid UUID format
      assertThat(response.getConversationId())
          .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("Response without sources returns empty sources list")
    void responseWithoutSourcesReturnsEmptyList() {
      ChatRequest request = ChatRequest.builder().question("General question").build();

      when(streamingRagService.chatStream(any(ChatRequest.class)))
          .thenReturn(Flux.just("Simple answer with no sources."));

      ChatResponse response = ragService.chat(request);

      assertThat(response.getAnswer()).isEqualTo("Simple answer with no sources.");
      assertThat(response.getSources()).isEmpty();
    }
  }

  // ========================================================================
  // Edge Cases
  // ========================================================================

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {

    @Test
    @DisplayName("Returns no-results response when streaming returns null")
    void returnsNoResultsWhenNull() {
      ChatRequest request = ChatRequest.builder().question("Obscure question").build();

      when(streamingRagService.chatStream(any(ChatRequest.class))).thenReturn(Flux.empty());

      ChatResponse response = ragService.chat(request);

      assertThat(response.getAnswer()).contains("could not find relevant information");
      assertThat(response.getSources()).isEmpty();
    }

    @Test
    @DisplayName("Joins multiple streaming tokens into complete response")
    void joinsMultipleTokens() {
      ChatRequest request = ChatRequest.builder().question("Test question").build();

      when(streamingRagService.chatStream(any(ChatRequest.class)))
          .thenReturn(Flux.just("Token1 ", "Token2 ", "Token3"));

      ChatResponse response = ragService.chat(request);

      assertThat(response.getAnswer()).isEqualTo("Token1 Token2 Token3");
    }
  }

  // ========================================================================
  // Error Scenarios
  // ========================================================================

  @Nested
  @DisplayName("Error Scenarios")
  class ErrorTests {

    @Test
    @DisplayName("Propagates exception from streaming service")
    void propagatesStreamingException() {
      ChatRequest request = ChatRequest.builder().question("Error question").build();

      when(streamingRagService.chatStream(any(ChatRequest.class)))
          .thenReturn(Flux.error(new RuntimeException("LLM unavailable")));

      assertThatThrownBy(() -> ragService.chat(request))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("LLM unavailable");
    }
  }
}
