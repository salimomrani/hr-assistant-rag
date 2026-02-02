package com.hrassistant.service;

import com.hrassistant.model.CachedResponse;
import com.hrassistant.model.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Decorator service that wraps StreamingRagService with semantic caching.
 * Checks cache before executing RAG pipeline and caches successful responses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CachingStreamingRagService {

    private final StreamingRagService streamingRagService;
    private final CacheService cacheService;

    /**
     * Processes a chat request with semantic caching.
     *
     * Flow:
     * 1. Check cache for semantically similar question (only if no document filter applied)
     * 2. If cache hit: return cached response as stream
     * 3. If cache miss: execute RAG pipeline, cache result, return stream
     *
     * Note: Caching is bypassed when documentIds filter is applied to ensure
     * filtered queries always return fresh results from the specified documents.
     *
     * @param request The chat request containing the user's question
     * @return Flux of response tokens (cached or freshly generated)
     */
    public Flux<String> chatStream(ChatRequest request) {
        String question = request.getQuestion();
        List<String> documentIds = request.getDocumentIds();
        log.debug("Processing request with cache check: {} (documentIds={})", question, documentIds);

        // Skip cache when document filter is applied
        boolean hasDocumentFilter = documentIds != null && !documentIds.isEmpty();
        if (hasDocumentFilter) {
            log.debug("Document filter applied, bypassing cache");
            return streamingRagService.chatStream(request);
        }

        // Step 1: Check cache for similar question
        Optional<CachedResponse> cached = cacheService.findSimilarCached(question);

        if (cached.isPresent()) {
            // Cache hit - return cached response as stream
            log.info("Serving from cache for question: {}", question);
            return streamCachedResponse(cached.get());
        }

        // Cache miss - execute RAG pipeline and cache the result
        log.debug("Cache miss, executing RAG pipeline for question: {}", question);
        return executeAndCache(request);
    }

    /**
     * Streams a cached response, simulating the token-by-token delivery.
     * Splits the response into words to maintain consistent streaming behavior.
     *
     * @param cached The cached response to stream
     * @return Flux of response tokens
     */
    private Flux<String> streamCachedResponse(CachedResponse cached) {
        if (!StringUtils.hasText(cached.getResponse())) {
            log.warn("Cached response is null or empty, returning empty flux");
            return Flux.empty();
        }

        // Split response into tokens (words) for streaming effect
        String[] tokens = cached.getResponse().split("(?<=\\s)");

        return Flux.fromArray(tokens)
                .delayElements(Duration.ofMillis(10))  // Small delay to simulate streaming
                .doOnComplete(() -> log.debug("Cached response streaming completed"));
    }

    /**
     * Executes the RAG pipeline and caches the result.
     * Uses reactive operators to safely collect tokens and cache the response.
     * Caching runs on a separate bounded elastic thread to avoid blocking reactor threads.
     *
     * @param request The chat request
     * @return Flux of response tokens
     */
    private Flux<String> executeAndCache(ChatRequest request) {
        return streamingRagService.chatStream(request)
                .collectList()
                .flatMapMany(tokens -> {
                    String fullResponse = String.join("", tokens);
                    List<String> sources = extractSourcesFromResponse(fullResponse);

                    // Run caching on a separate thread to avoid blocking reactor threads
                    // EmbeddingModel.embed() is a blocking call
                    Schedulers.boundedElastic().schedule(() -> {
                        try {
                            cacheService.cacheResponse(request.getQuestion(), fullResponse, sources);
                        } catch (Exception e) {
                            log.warn("Failed to cache response asynchronously: {}", e.getMessage());
                        }
                    });

                    return Flux.fromIterable(tokens);
                })
                .onErrorResume(error -> {
                    log.debug("RAG pipeline error, not caching: {}", error.getMessage());
                    return streamingRagService.chatStream(request);
                });
    }

    /**
     * Extracts source names from the response text.
     * Sources are appended at the end in format "**Sources:**\n- source1\n- source2"
     *
     * @param response The full response text
     * @return List of source names
     */
    private List<String> extractSourcesFromResponse(String response) {
        String sourcesMarker = "**Sources:**";
        int sourcesIndex = response.indexOf(sourcesMarker);

        if (sourcesIndex == -1) {
            return List.of();
        }

        String sourcesSection = response.substring(sourcesIndex + sourcesMarker.length());
        return sourcesSection.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("- "))
                .map(line -> line.substring(2).trim())
                .toList();
    }
}
