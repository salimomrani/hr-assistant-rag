package com.hrassistant.service;

import com.hrassistant.model.CachedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service for semantic caching of LLM responses.
 * Uses embeddings to find semantically similar questions and returns cached responses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private static final String CACHE_KEY_PREFIX = "hr-assistant:cache:";

    private final RedisTemplate<String, CachedResponse> redisTemplate;
    private final EmbeddingModel embeddingModel;

    @Value("${hr-assistant.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${hr-assistant.cache.ttl-seconds:3600}")
    private int ttlSeconds;

    @Value("${hr-assistant.cache.similarity-threshold:0.85}")
    private double similarityThreshold;

    /**
     * Searches for a cached response that is semantically similar to the given question.
     * Uses cosine similarity to compare question embeddings.
     *
     * @param question The user's question
     * @return Optional containing the cached response if a similar question was found
     */
    public Optional<CachedResponse> findSimilarCached(String question) {
        if (!cacheEnabled) {
            log.debug("Cache is disabled, skipping cache lookup");
            return Optional.empty();
        }

        try {
            // Generate embedding for the incoming question
            float[] queryEmbedding = generateEmbedding(question);

            // Scan all cached responses
            Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            if (CollectionUtils.isEmpty(keys)) {
                log.debug("Cache is empty, no cached responses to compare");
                return Optional.empty();
            }

            double bestSimilarity = 0.0;
            CachedResponse bestMatch = null;

            for (String key : keys) {
                CachedResponse cached = redisTemplate.opsForValue().get(key);
                if (cached == null || cached.getQuestionEmbedding() == null) {
                    continue;
                }

                double similarity = cosineSimilarity(queryEmbedding, cached.getQuestionEmbedding());

                if (similarity > similarityThreshold && similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                    bestMatch = cached;
                }
            }

            if (bestMatch != null) {
                log.info("Cache HIT: similarity={} for question: {}",
                        String.format("%.4f", bestSimilarity), question);
                return Optional.of(bestMatch);
            }

            log.debug("Cache MISS: no similar question found (checked {} entries)", keys.size());
            return Optional.empty();

        } catch (Exception e) {
            log.warn("Cache lookup failed: {}, proceeding without cache", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Caches a response for future semantic matching.
     *
     * @param question The user's question
     * @param response The complete LLM response
     * @param sources  The sources used to generate the response
     */
    public void cacheResponse(String question, String response, List<String> sources) {
        if (!cacheEnabled) {
            log.debug("Cache is disabled, skipping cache storage");
            return;
        }

        try {
            float[] embedding = generateEmbedding(question);

            CachedResponse cached = CachedResponse.builder()
                    .question(question)
                    .questionEmbedding(embedding)
                    .response(response)
                    .sources(sources)
                    .cachedAt(LocalDateTime.now())
                    .build();

            String key = CACHE_KEY_PREFIX + UUID.randomUUID();

            redisTemplate.opsForValue().set(key, cached, Duration.ofSeconds(ttlSeconds));

            log.info("Cached response for question: {} (TTL: {}s)", question, ttlSeconds);

        } catch (Exception e) {
            log.warn("Failed to cache response: {}", e.getMessage());
            // Don't throw - caching failures should not break the main flow
        }
    }

    /**
     * Invalidates all cached responses.
     * Called when documents are uploaded or deleted.
     */
    public void invalidateAll() {
        if (!cacheEnabled) {
            return;
        }

        try {
            Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            if (CollectionUtils.isEmpty(keys)) {
                log.debug("Cache invalidation: no entries to clear");
                return;
            }
            Long deletedCount = redisTemplate.delete(keys);
            log.info("Cache INVALIDATED: {} entries cleared", deletedCount);
        } catch (Exception e) {
            log.warn("Failed to invalidate cache: {}", e.getMessage());
        }
    }

    /**
     * Generates an embedding for the given text using Spring AI's EmbeddingModel.
     *
     * @param text The text to embed
     * @return The embedding as a float array
     */
    private float[] generateEmbedding(String text) {
        return embeddingModel.embed(text);
    }

    /**
     * Calculates the cosine similarity between two vectors.
     *
     * @param a First vector
     * @param b Second vector
     * @return Cosine similarity (0 to 1, where 1 means identical)
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
