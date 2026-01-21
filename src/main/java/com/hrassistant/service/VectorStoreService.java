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

import java.util.List;

/**
 * @author : salimomrani
 * @email : omrani_salim@outlook.fr
 * @created : 21/01/2026, mercredi
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final InMemoryEmbeddingStore<TextSegment> embeddingStore;

    @Value("${hr-assistant.rag.max-results:5}")
    private int maxResults;

    @Value("${hr-assistant.rag.similarity-threshold:0.3}")
    private double minScore;

    public void store(Embedding embedding, TextSegment segment) {
        log.debug("Storing embedding for document: {}",
                segment.metadata().getString("documentName"));
        embeddingStore.add(embedding, segment);
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
}
