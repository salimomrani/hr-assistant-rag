package com.hrassistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;

    @Value("${hr-assistant.rag.max-results:5}")
    private int maxResults;

    @Value("${hr-assistant.rag.similarity-threshold:0.3}")
    private double minScore;

    /**
     * Stores a document in the vector store.
     * Spring AI automatically generates embeddings and persists to PostgreSQL.
     */
    public void store(Document document) {
        log.debug("Storing document {} for source: {}",
                document.getId(), document.getMetadata().get("documentName"));

        // Store in vector store (embedding is generated automatically)
        vectorStore.add(List.of(document));
    }

    /**
     * Searches for similar documents.
     */
    public List<Document> search(String query) {
        log.debug("Searching for similar documents (maxResults={}, minScore={})",
                maxResults, minScore);

        List<Document> matches = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(maxResults)
                        .similarityThreshold(minScore)
                        .build()
        );

        log.debug("Found {} matching documents", matches.size());
        return matches;
    }

    /**
     * Removes all documents associated with a source document ID.
     * Uses filter expression to delete from PostgreSQL by metadata.
     */
    public void removeByDocumentId(String documentId) {
        log.debug("Removing documents for source: {}", documentId);

        // Build filter expression: documentId == 'value'
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        Filter.Expression filterExpression = builder.eq("documentId", documentId).build();

        // Delete using filter expression
        vectorStore.delete(filterExpression);

        log.info("Removed documents for source: {}", documentId);
    }
}
