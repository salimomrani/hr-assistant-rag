package com.hrassistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

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
     * Searches for similar documents across all indexed documents.
     */
    public List<Document> search(String query) {
        return search(query, null);
    }

    /**
     * Searches for similar documents with optional filtering by document IDs.
     *
     * @param query The search query
     * @param documentIds Optional list of document IDs to restrict search. If null or empty, searches all documents.
     * @return List of matching documents
     */
    public List<Document> search(String query, List<String> documentIds) {
        log.debug("Searching for similar documents (maxResults={}, minScore={}, documentIds={})",
                maxResults, minScore, documentIds);

        SearchRequest.Builder requestBuilder = SearchRequest.builder()
                .query(query)
                .topK(maxResults)
                .similarityThreshold(minScore);

        // Apply document ID filter if specified
        if (documentIds != null && !documentIds.isEmpty()) {
            // Build filter expression using OR for multiple document IDs
            // Format: documentId == 'id1' || documentId == 'id2' || ...
            String filterExpression = documentIds.stream()
                    .map(id -> "documentId == '" + id + "'")
                    .reduce((a, b) -> a + " || " + b)
                    .orElse("");

            requestBuilder.filterExpression(filterExpression);
            log.debug("Applied document filter expression: {}", filterExpression);
        }

        List<Document> matches = vectorStore.similaritySearch(requestBuilder.build());

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

    /**
     * Updates the document name in vector store metadata for all chunks of a document.
     * Uses native SQL to update the JSON metadata column in PostgreSQL.
     * Note: The vector_store table uses JSON type (not JSONB), so we cast to JSONB, update, and cast back.
     *
     * @param documentId The document ID
     * @param newDocumentName The new document name
     */
    public void updateDocumentName(String documentId, String newDocumentName) {
        log.debug("Updating document name in vector store for documentId: {}", documentId);

        // Update metadata JSON field in vector_store table
        // Cast json to jsonb, perform update, cast back to json
        String sql = """
            UPDATE vector_store
            SET metadata = jsonb_set(metadata::jsonb, '{documentName}', to_jsonb(?::text))::json
            WHERE metadata->>'documentId' = ?
            """;

        int updatedRows = jdbcTemplate.update(sql, newDocumentName, documentId);

        log.info("Updated document name in {} vector store entries for documentId: {}", updatedRows, documentId);
    }
}
