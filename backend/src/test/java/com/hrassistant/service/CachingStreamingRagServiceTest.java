package com.hrassistant.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hrassistant.model.CachedResponse;
import com.hrassistant.model.ChatRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CachingStreamingRagServiceTest {

  @Mock private StreamingRagService streamingRagService;
  @Mock private CacheService cacheService;

  @InjectMocks private CachingStreamingRagService cachingStreamingRagService;

  // ========================================================================
  // Cache Hit
  // ========================================================================

  @Nested
  @DisplayName("Cache Hit")
  class CacheHitTests {

    @Test
    @DisplayName("Returns cached response when cache hit found")
    void returnsCachedResponseOnHit() {
      ChatRequest request = ChatRequest.builder().question("How many vacation days?").build();

      CachedResponse cached =
          CachedResponse.builder()
              .question("How many vacation days?")
              .response("You have 25 days per year.")
              .sources(List.of("conges.pdf"))
              .cachedAt(LocalDateTime.now())
              .build();

      when(cacheService.findSimilarCached(anyString())).thenReturn(Optional.of(cached));

      StepVerifier.create(cachingStreamingRagService.chatStream(request))
          .thenConsumeWhile(token -> true) // consume all tokens from word splitting
          .verifyComplete();

      // Should not call streaming service on cache hit
      verify(streamingRagService, never()).chatStream(any(ChatRequest.class));
    }

    @Test
    @DisplayName("Cached response with empty text returns empty flux")
    void cachedEmptyResponseReturnsEmptyFlux() {
      ChatRequest request = ChatRequest.builder().question("Question").build();

      CachedResponse cached =
          CachedResponse.builder()
              .question("Question")
              .response("")
              .sources(List.of())
              .cachedAt(LocalDateTime.now())
              .build();

      when(cacheService.findSimilarCached(anyString())).thenReturn(Optional.of(cached));

      StepVerifier.create(cachingStreamingRagService.chatStream(request)).verifyComplete();
    }
  }

  // ========================================================================
  // Cache Miss
  // ========================================================================

  @Nested
  @DisplayName("Cache Miss")
  class CacheMissTests {

    @Test
    @DisplayName("Delegates to streaming service on cache miss")
    void delegatesToStreamingServiceOnMiss() {
      ChatRequest request = ChatRequest.builder().question("New question").build();

      when(cacheService.findSimilarCached(anyString())).thenReturn(Optional.empty());
      when(streamingRagService.chatStream(any(ChatRequest.class)))
          .thenReturn(Flux.just("Fresh ", "response."));

      StepVerifier.create(cachingStreamingRagService.chatStream(request))
          .expectNext("Fresh ")
          .expectNext("response.")
          .verifyComplete();
    }

    @Test
    @DisplayName("Caches response after successful streaming completion")
    void cachesResponseAfterStreaming() throws InterruptedException {
      ChatRequest request = ChatRequest.builder().question("Cacheable question").build();

      when(cacheService.findSimilarCached(anyString())).thenReturn(Optional.empty());
      when(streamingRagService.chatStream(any(ChatRequest.class))).thenReturn(Flux.just("Answer"));

      // Consume the flux to trigger doOnComplete
      cachingStreamingRagService.chatStream(request).collectList().block();

      // Allow async cache operation to execute
      Thread.sleep(200);

      verify(cacheService).cacheResponse(anyString(), anyString(), any());
    }
  }

  // ========================================================================
  // Document Filter Bypass
  // ========================================================================

  @Nested
  @DisplayName("Document Filter Bypass")
  class DocumentFilterBypassTests {

    @Test
    @DisplayName("Bypasses cache when documentIds filter is applied")
    void bypassesCacheWithDocumentFilter() {
      ChatRequest request =
          ChatRequest.builder().question("Filtered question").documentIds(List.of("doc-1")).build();

      when(streamingRagService.chatStream(any(ChatRequest.class)))
          .thenReturn(Flux.just("Filtered response."));

      StepVerifier.create(cachingStreamingRagService.chatStream(request))
          .expectNext("Filtered response.")
          .verifyComplete();

      // Cache should not be consulted
      verify(cacheService, never()).findSimilarCached(anyString());
    }

    @Test
    @DisplayName("Does not bypass cache when documentIds is null")
    void doesNotBypassCacheWhenDocumentIdsNull() {
      ChatRequest request = ChatRequest.builder().question("Normal question").build();

      when(cacheService.findSimilarCached(anyString())).thenReturn(Optional.empty());
      when(streamingRagService.chatStream(any(ChatRequest.class)))
          .thenReturn(Flux.just("Response"));

      cachingStreamingRagService.chatStream(request).collectList().block();

      verify(cacheService).findSimilarCached("Normal question");
    }

    @Test
    @DisplayName("Does not bypass cache when documentIds is empty list")
    void doesNotBypassCacheWhenDocumentIdsEmpty() {
      ChatRequest request =
          ChatRequest.builder().question("Another question").documentIds(List.of()).build();

      when(cacheService.findSimilarCached(anyString())).thenReturn(Optional.empty());
      when(streamingRagService.chatStream(any(ChatRequest.class)))
          .thenReturn(Flux.just("Response"));

      cachingStreamingRagService.chatStream(request).collectList().block();

      verify(cacheService).findSimilarCached("Another question");
    }
  }
}
