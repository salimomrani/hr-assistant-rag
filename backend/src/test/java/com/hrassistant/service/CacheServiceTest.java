package com.hrassistant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hrassistant.model.CachedResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

  @Mock private RedisTemplate<String, CachedResponse> redisTemplate;
  @Mock private EmbeddingModel embeddingModel;
  @Mock private ValueOperations<String, CachedResponse> valueOperations;

  private CacheService cacheService;

  @BeforeEach
  void setUp() {
    cacheService = new CacheService(redisTemplate, embeddingModel);
    ReflectionTestUtils.setField(cacheService, "cacheEnabled", true);
    ReflectionTestUtils.setField(cacheService, "ttlSeconds", 3600);
    ReflectionTestUtils.setField(cacheService, "similarityThreshold", 0.85);
  }

  // ========================================================================
  // findSimilarCached
  // ========================================================================

  @Nested
  @DisplayName("findSimilarCached")
  class FindSimilarCachedTests {

    @Test
    @DisplayName("Returns empty when cache is disabled")
    void returnsEmptyWhenDisabled() {
      ReflectionTestUtils.setField(cacheService, "cacheEnabled", false);

      Optional<CachedResponse> result = cacheService.findSimilarCached("test question");

      assertThat(result).isEmpty();
      verify(embeddingModel, never()).embed(anyString());
    }

    @Test
    @DisplayName("Returns empty when no cache keys exist")
    void returnsEmptyWhenNoCacheKeys() {
      when(embeddingModel.embed(anyString())).thenReturn(new float[] {1.0f, 0.0f, 0.0f});
      when(redisTemplate.keys(anyString())).thenReturn(Set.of());

      Optional<CachedResponse> result = cacheService.findSimilarCached("test question");

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Returns cached response when similarity exceeds threshold")
    void returnsCachedWhenSimilarityExceedsThreshold() {
      float[] queryEmbedding = {1.0f, 0.0f, 0.0f};
      float[] cachedEmbedding = {0.99f, 0.1f, 0.0f}; // very similar

      CachedResponse cached =
          CachedResponse.builder()
              .question("How many vacation days?")
              .questionEmbedding(cachedEmbedding)
              .response("You have 25 days.")
              .sources(List.of("conges.pdf"))
              .cachedAt(LocalDateTime.now())
              .build();

      when(embeddingModel.embed(anyString())).thenReturn(queryEmbedding);
      when(redisTemplate.keys(anyString())).thenReturn(Set.of("hr-assistant:cache:abc"));
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      when(valueOperations.get("hr-assistant:cache:abc")).thenReturn(cached);

      Optional<CachedResponse> result = cacheService.findSimilarCached("Vacation days?");

      assertThat(result).isPresent();
      assertThat(result.get().getResponse()).isEqualTo("You have 25 days.");
    }

    @Test
    @DisplayName("Returns empty when similarity is below threshold")
    void returnsEmptyWhenSimilarityBelowThreshold() {
      float[] queryEmbedding = {1.0f, 0.0f, 0.0f};
      float[] cachedEmbedding = {0.0f, 1.0f, 0.0f}; // orthogonal = 0 similarity

      CachedResponse cached =
          CachedResponse.builder()
              .question("Unrelated question")
              .questionEmbedding(cachedEmbedding)
              .response("Unrelated answer.")
              .sources(List.of())
              .cachedAt(LocalDateTime.now())
              .build();

      when(embeddingModel.embed(anyString())).thenReturn(queryEmbedding);
      when(redisTemplate.keys(anyString())).thenReturn(Set.of("hr-assistant:cache:xyz"));
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      when(valueOperations.get("hr-assistant:cache:xyz")).thenReturn(cached);

      Optional<CachedResponse> result = cacheService.findSimilarCached("Totally different");

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Returns empty on Redis exception without propagating")
    void returnsEmptyOnException() {
      when(embeddingModel.embed(anyString())).thenReturn(new float[] {1.0f});
      when(redisTemplate.keys(anyString())).thenThrow(new RuntimeException("Redis down"));

      Optional<CachedResponse> result = cacheService.findSimilarCached("question");

      assertThat(result).isEmpty();
    }
  }

  // ========================================================================
  // cacheResponse
  // ========================================================================

  @Nested
  @DisplayName("cacheResponse")
  class CacheResponseTests {

    @Test
    @DisplayName("Stores response with TTL when cache is enabled")
    void storesResponseWithTtl() {
      float[] embedding = {1.0f, 0.5f, 0.0f};
      when(embeddingModel.embed(anyString())).thenReturn(embedding);
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);

      cacheService.cacheResponse("Test question", "Test response", List.of("doc.pdf"));

      verify(valueOperations)
          .set(anyString(), any(CachedResponse.class), eq(Duration.ofSeconds(3600)));
    }

    @Test
    @DisplayName("Does not store when cache is disabled")
    void doesNotStoreWhenDisabled() {
      ReflectionTestUtils.setField(cacheService, "cacheEnabled", false);

      cacheService.cacheResponse("Test question", "Test response", List.of());

      verify(embeddingModel, never()).embed(anyString());
    }

    @Test
    @DisplayName("Swallows exception without propagating")
    void swallowsExceptionGracefully() {
      when(embeddingModel.embed(anyString())).thenThrow(new RuntimeException("Embedding failed"));

      // Should not throw
      cacheService.cacheResponse("Test question", "Test response", List.of());
    }
  }

  // ========================================================================
  // invalidateAll
  // ========================================================================

  @Nested
  @DisplayName("invalidateAll")
  class InvalidateAllTests {

    @Test
    @DisplayName("Deletes all cache keys")
    void deletesAllCacheKeys() {
      Set<String> keys = Set.of("hr-assistant:cache:1", "hr-assistant:cache:2");
      when(redisTemplate.keys("hr-assistant:cache:*")).thenReturn(keys);
      when(redisTemplate.delete(keys)).thenReturn(2L);

      cacheService.invalidateAll();

      verify(redisTemplate).delete(keys);
    }

    @Test
    @DisplayName("Does nothing when cache is disabled")
    void doesNothingWhenDisabled() {
      ReflectionTestUtils.setField(cacheService, "cacheEnabled", false);

      cacheService.invalidateAll();

      verify(redisTemplate, never()).keys(anyString());
    }

    @Test
    @DisplayName("Does nothing when no keys exist")
    void doesNothingWhenNoKeys() {
      when(redisTemplate.keys(anyString())).thenReturn(Set.of());

      cacheService.invalidateAll();

      verify(redisTemplate, never()).delete(any(Set.class));
    }
  }
}
