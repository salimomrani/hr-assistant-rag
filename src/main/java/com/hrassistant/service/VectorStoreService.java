package com.hrassistant.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final InMemoryEmbeddingStore<TextSegment> embeddingStore;

    // Track embedding IDs by document ID for deletion
    private final Map<String, List<String>> documentEmbeddings = new ConcurrentHashMap<>();

    @Value("${hr-assistant.rag.max-results:5}")
    private int maxResults;

    @Value("${hr-assistant.rag.similarity-threshold:0.3}")
    private double minScore;

    public void store(Embedding embedding, TextSegment segment) {
        String documentId = segment.metadata().getString("documentId");
        String embeddingId = UUID.randomUUID().toString();

        log.debug("Storing embedding {} for document: {}",
                embeddingId, segment.metadata().getString("documentName"));

        // Store in vector store with ID
        embeddingStore.add(embeddingId, embedding, segment);

        // Track embedding ID for this document
        documentEmbeddings.computeIfAbsent(documentId, k -> new ArrayList<>()).add(embeddingId);
    }

    public List<EmbeddingMatch<TextSegment>> search(Embedding queryEmbedding) {
        log.debug("Searching for similar embeddings (maxResults={}, minScore={})",
                maxResults, minScore);

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(request).matches();
        log.debug("Found {} matching segments", matches.size());

        return matches;
    }

    /**
     * Removes all embeddings associated with a document.
     *
     * @param documentId The document ID
     */
    public void removeByDocumentId(String documentId) {
        List<String> embeddingIds = documentEmbeddings.remove(documentId);

        if (embeddingIds != null && !embeddingIds.isEmpty()) {
            log.debug("Removing {} embeddings for document: {}", embeddingIds.size(), documentId);
            embeddingStore.removeAll(embeddingIds);
            log.info("Removed {} embeddings for document: {}", embeddingIds.size(), documentId);
        } else {
            log.debug("No embeddings found for document: {}", documentId);
        }
    }
}
