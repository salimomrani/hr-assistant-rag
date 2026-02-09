package com.hrassistant.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VectorStoreMetadataRepository {

  private final JdbcTemplate jdbcTemplate;

  /**
   * Updates the document name in vector store metadata for all chunks of a document. The
   * vector_store table uses JSON type (not JSONB), so we cast to JSONB, update, and cast back.
   *
   * @param documentId The document ID
   * @param newDocumentName The new document name
   * @return The number of updated rows
   */
  public int updateDocumentName(String documentId, String newDocumentName) {
    String sql =
        """
        UPDATE vector_store
        SET metadata = jsonb_set(metadata::jsonb, '{documentName}', to_jsonb(?::text))::json
        WHERE metadata->>'documentId' = ?
        """;
    return jdbcTemplate.update(sql, newDocumentName, documentId);
  }
}
