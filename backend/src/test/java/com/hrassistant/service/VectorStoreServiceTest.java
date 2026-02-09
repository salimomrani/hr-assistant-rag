package com.hrassistant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VectorStoreServiceTest {

  @Mock private VectorStore vectorStore;
  @Mock private JdbcTemplate jdbcTemplate;

  private VectorStoreService vectorStoreService;

  @BeforeEach
  void setUp() {
    vectorStoreService = new VectorStoreService(vectorStore, jdbcTemplate);
    ReflectionTestUtils.setField(vectorStoreService, "maxResults", 5);
    ReflectionTestUtils.setField(vectorStoreService, "minScore", 0.3);
  }

  // ========================================================================
  // store
  // ========================================================================

  @Nested
  @DisplayName("store")
  class StoreTests {

    @Test
    @DisplayName("Stores document in vector store")
    void storesDocumentInVectorStore() {
      Document doc =
          new Document("chunk-1", "HR policy content", Map.of("documentName", "policy.pdf"));

      vectorStoreService.store(doc);

      verify(vectorStore).add(List.of(doc));
    }
  }

  // ========================================================================
  // search
  // ========================================================================

  @Nested
  @DisplayName("search")
  class SearchTests {

    @Test
    @DisplayName("Search without filter returns matching documents")
    void searchWithoutFilterReturnsDocuments() {
      Document match =
          new Document("Vacation policy content", Map.of("documentName", "conges.pdf"));
      when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(match));

      List<Document> results = vectorStoreService.search("How many vacation days?");

      assertThat(results).hasSize(1);
      assertThat(results.get(0).getText()).isEqualTo("Vacation policy content");
    }

    @Test
    @DisplayName("Search with null documentIds searches all documents")
    void searchWithNullDocumentIdsSearchesAll() {
      when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

      List<Document> results = vectorStoreService.search("test query", null);

      assertThat(results).isEmpty();
      // Verify search was called (no filter applied)
      verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("Search with empty documentIds searches all documents")
    void searchWithEmptyDocumentIdsSearchesAll() {
      when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

      List<Document> results = vectorStoreService.search("test query", List.of());

      assertThat(results).isEmpty();
      verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("Search with documentIds applies filter")
    void searchWithDocumentIdsAppliesFilter() {
      Document match =
          new Document("Filtered content", Map.of("documentName", "doc.pdf", "documentId", "d1"));
      when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(match));

      List<Document> results = vectorStoreService.search("filtered query", List.of("d1", "d2"));

      assertThat(results).hasSize(1);

      // Verify the search request was built with filter
      ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
      verify(vectorStore).similaritySearch(captor.capture());
      SearchRequest capturedRequest = captor.getValue();
      assertThat(capturedRequest.getFilterExpression()).isNotNull();
    }

    @Test
    @DisplayName("Returns empty list when no matches found")
    void returnsEmptyListWhenNoMatches() {
      when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

      List<Document> results = vectorStoreService.search("no match");

      assertThat(results).isEmpty();
    }
  }

  // ========================================================================
  // removeByDocumentId
  // ========================================================================

  @Nested
  @DisplayName("removeByDocumentId")
  class RemoveByDocumentIdTests {

    @Test
    @DisplayName("Removes documents by documentId filter expression")
    void removesDocumentsByFilter() {
      vectorStoreService.removeByDocumentId("doc-123");

      verify(vectorStore).delete(any(Filter.Expression.class));
    }
  }

  // ========================================================================
  // updateDocumentName
  // ========================================================================

  @Nested
  @DisplayName("updateDocumentName")
  class UpdateDocumentNameTests {

    @Test
    @DisplayName("Updates document name using native SQL")
    void updatesDocumentNameViaSql() {
      when(jdbcTemplate.update(anyString(), anyString(), anyString())).thenReturn(3);

      vectorStoreService.updateDocumentName("doc-123", "new-name.pdf");

      verify(jdbcTemplate).update(anyString(), eq("new-name.pdf"), eq("doc-123"));
    }

    @Test
    @DisplayName("Handles zero rows updated gracefully")
    void handlesZeroRowsUpdated() {
      when(jdbcTemplate.update(anyString(), anyString(), anyString())).thenReturn(0);

      // Should not throw
      vectorStoreService.updateDocumentName("nonexistent", "new.pdf");

      verify(jdbcTemplate).update(anyString(), eq("new.pdf"), eq("nonexistent"));
    }
  }
}
