package com.hrassistant.service;

import com.hrassistant.model.CachedResponse;
import com.hrassistant.model.ChatInteraction;
import com.hrassistant.model.ChatRequest;
import com.hrassistant.model.Document;
import com.hrassistant.repository.ChatInteractionRepository;
import com.hrassistant.repository.DocumentRepository;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * Decorator service that wraps StreamingRagService with semantic caching. Checks cache before
 * executing RAG pipeline and caches successful responses. Also persists ChatInteraction for
 * analytics tracking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CachingStreamingRagService {

  private final StreamingRagService streamingRagService;
  private final CacheService cacheService;
  private final ChatInteractionRepository chatInteractionRepository;
  private final DocumentRepository documentRepository;

  /**
   * Processes a chat request with semantic caching.
   *
   * <p>Flow: 1. Check cache for semantically similar question (only if no document filter applied)
   * 2. If cache hit: return cached response as stream 3. If cache miss: execute RAG pipeline, cache
   * result, return stream
   *
   * <p>Note: Caching is bypassed when documentIds filter is applied to ensure filtered queries
   * always return fresh results from the specified documents.
   *
   * @param request The chat request containing the user's question
   * @return Flux of response tokens (cached or freshly generated)
   */
  public Flux<String> chatStream(ChatRequest request) {
    String question = request.getQuestion();
    List<String> documentIds = request.getDocumentIds();
    long startTime = System.currentTimeMillis();
    log.debug("Processing request with cache check: {} (documentIds={})", question, documentIds);

    // Skip cache when document filter is applied
    boolean hasDocumentFilter = documentIds != null && !documentIds.isEmpty();
    if (hasDocumentFilter) {
      log.debug("Document filter applied, bypassing cache");
      return wrapWithInteractionTracking(
          streamingRagService.chatStream(request), request, startTime);
    }

    // Step 1: Check cache for similar question
    Optional<CachedResponse> cached = cacheService.findSimilarCached(question);

    if (cached.isPresent()) {
      // Cache hit - return cached response as stream
      log.info("Serving from cache for question: {}", question);
      return wrapWithInteractionTracking(streamCachedResponse(cached.get()), request, startTime);
    }

    // Cache miss - execute RAG pipeline and cache the result
    log.debug("Cache miss, executing RAG pipeline for question: {}", question);
    return executeAndCache(request, startTime);
  }

  /**
   * Streams a cached response, simulating the token-by-token delivery. Splits the response into
   * words to maintain consistent streaming behavior.
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
        .delayElements(Duration.ofMillis(10)) // Small delay to simulate streaming
        .doOnComplete(() -> log.debug("Cached response streaming completed"));
  }

  /**
   * Executes the RAG pipeline and caches the result. Uses reactive operators to safely collect
   * tokens and cache the response. Caching runs on a separate bounded elastic thread to avoid
   * blocking reactor threads.
   *
   * @param request The chat request
   * @param startTime The start time for response time tracking
   * @return Flux of response tokens
   */
  private Flux<String> executeAndCache(ChatRequest request, long startTime) {
    // Use a buffer to collect tokens for caching without blocking the stream
    List<String> tokensBuffer = new java.util.ArrayList<>();

    return streamingRagService
        .chatStream(request)
        .doOnNext(tokensBuffer::add) // Add each token to the buffer
        .doOnComplete(
            () -> {
              // When streaming is complete, cache the full response and persist interaction
              String fullResponse = String.join("", tokensBuffer);
              if (StringUtils.hasText(fullResponse)) {
                List<String> sources = SourceParsingUtil.extractSources(fullResponse);

                // Run caching and interaction persistence on a separate thread
                Schedulers.boundedElastic()
                    .schedule(
                        () -> {
                          try {
                            cacheService.cacheResponse(
                                request.getQuestion(), fullResponse, sources);
                          } catch (Exception e) {
                            log.warn("Failed to cache response asynchronously: {}", e.getMessage());
                          }
                          persistInteraction(request, startTime, sources);
                        });
              }
            })
        .doOnError(error -> log.debug("RAG pipeline error, not caching: {}", error.getMessage()));
  }

  /** Wraps a flux with interaction tracking for cache hits and filtered queries. */
  private Flux<String> wrapWithInteractionTracking(
      Flux<String> flux, ChatRequest request, long startTime) {
    List<String> tokensBuffer = new java.util.ArrayList<>();

    return flux.doOnNext(tokensBuffer::add)
        .doOnComplete(
            () -> {
              String fullResponse = String.join("", tokensBuffer);
              List<String> sources = SourceParsingUtil.extractSources(fullResponse);
              Schedulers.boundedElastic()
                  .schedule(() -> persistInteraction(request, startTime, sources));
            });
  }

  /**
   * Persists a ChatInteraction record for analytics. Resolves document names from sources to
   * document IDs.
   */
  private void persistInteraction(ChatRequest request, long startTime, List<String> sourceNames) {
    try {
      long responseTimeMs = System.currentTimeMillis() - startTime;

      List<String> documentIds = List.of();
      if (sourceNames != null && !sourceNames.isEmpty()) {
        documentIds =
            documentRepository.findByFilenameIn(sourceNames).stream().map(Document::getId).toList();
      }

      ChatInteraction interaction =
          ChatInteraction.builder()
              .question(request.getQuestion())
              .responseTimeMs(responseTimeMs)
              .referencedDocumentIds(documentIds)
              .build();

      chatInteractionRepository.save(interaction);
      log.debug(
          "Persisted chat interaction: question='{}', responseTimeMs={}, docs={}",
          request.getQuestion(),
          responseTimeMs,
          documentIds.size());
    } catch (Exception e) {
      log.warn("Failed to persist chat interaction: {}", e.getMessage());
    }
  }
}
