package com.hrassistant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hrassistant.exception.HrAssistantException;
import com.hrassistant.mapper.DocumentMapper;
import com.hrassistant.model.Document;
import com.hrassistant.model.DocumentInfo;
import com.hrassistant.model.DocumentStatus;
import com.hrassistant.model.DocumentType;
import com.hrassistant.repository.DocumentRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

  @Mock private EmbeddingService embeddingService;
  @Mock private VectorStoreService vectorStoreService;
  @Mock private DocumentRepository documentRepository;
  @Mock private DocumentMapper documentMapper;
  @Mock private CacheService cacheService;

  private DocumentService documentService;

  @TempDir java.nio.file.Path tempDir;

  @BeforeEach
  void setUp() {
    documentService =
        new DocumentService(
            embeddingService, vectorStoreService, documentRepository, documentMapper, cacheService);
    ReflectionTestUtils.setField(documentService, "maxSizeMb", 10);
    ReflectionTestUtils.setField(documentService, "chunkSize", 500);
    ReflectionTestUtils.setField(documentService, "chunkOverlap", 50);
    ReflectionTestUtils.setField(documentService, "storagePath", tempDir.toString());
  }

  // ========================================================================
  // uploadDocument
  // ========================================================================

  @Nested
  @DisplayName("uploadDocument")
  class UploadDocumentTests {

    @Test
    @DisplayName("Successfully uploads and indexes a TXT file")
    void uploadsAndIndexesTxtFile() throws IOException {
      MockMultipartFile file =
          new MockMultipartFile(
              "file", "policy.txt", "text/plain", "This is the HR policy content.".getBytes());

      DocumentInfo expectedInfo =
          DocumentInfo.builder()
              .id("test-id")
              .filename("policy.txt")
              .type(DocumentType.TXT)
              .status(DocumentStatus.INDEXED)
              .build();

      when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
      when(embeddingService.toDocument(any()))
          .thenReturn(new org.springframework.ai.document.Document("chunk content"));
      doNothing().when(vectorStoreService).store(any());
      doNothing().when(cacheService).invalidateAll();
      when(documentMapper.toDocumentInfo(any(Document.class))).thenReturn(expectedInfo);

      DocumentInfo result = documentService.uploadDocument(file, "HR");

      assertThat(result).isNotNull();
      assertThat(result.getFilename()).isEqualTo("policy.txt");
      verify(documentRepository, times(2)).save(any(Document.class));
      verify(cacheService).invalidateAll();
    }

    @Test
    @DisplayName("Throws on empty file")
    void throwsOnEmptyFile() {
      MockMultipartFile file =
          new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

      assertThatThrownBy(() -> documentService.uploadDocument(file, "HR"))
          .isInstanceOf(HrAssistantException.class)
          .satisfies(
              ex ->
                  assertThat(((HrAssistantException) ex).getErrorCode())
                      .isEqualTo(HrAssistantException.ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("Throws on unsupported file type")
    void throwsOnUnsupportedFileType() {
      MockMultipartFile file =
          new MockMultipartFile("file", "data.xlsx", "application/xlsx", "data".getBytes());

      assertThatThrownBy(() -> documentService.uploadDocument(file, "HR"))
          .isInstanceOf(HrAssistantException.class)
          .satisfies(
              ex ->
                  assertThat(((HrAssistantException) ex).getErrorCode())
                      .isEqualTo(HrAssistantException.ErrorCode.INVALID_INPUT));
    }

    @Test
    @DisplayName("Throws on file exceeding max size")
    void throwsOnFileTooLarge() {
      // Set max to 1MB
      ReflectionTestUtils.setField(documentService, "maxSizeMb", 1);

      byte[] largeContent = new byte[2 * 1024 * 1024]; // 2MB
      MockMultipartFile file =
          new MockMultipartFile("file", "large.txt", "text/plain", largeContent);

      assertThatThrownBy(() -> documentService.uploadDocument(file, "HR"))
          .isInstanceOf(HrAssistantException.class)
          .satisfies(
              ex ->
                  assertThat(((HrAssistantException) ex).getErrorCode())
                      .isEqualTo(HrAssistantException.ErrorCode.INVALID_INPUT));
    }
  }

  // ========================================================================
  // deleteDocument
  // ========================================================================

  @Nested
  @DisplayName("deleteDocument")
  class DeleteDocumentTests {

    @Test
    @DisplayName("Deletes document from DB, vector store, and invalidates cache")
    void deletesDocumentCompletely() {
      Document doc =
          Document.builder()
              .id("doc-1")
              .filename("policy.pdf")
              .type(DocumentType.PDF)
              .status(DocumentStatus.INDEXED)
              .filePath("doc-1.pdf")
              .build();

      when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));

      documentService.deleteDocument("doc-1");

      verify(documentRepository).delete(doc);
      verify(vectorStoreService).removeByDocumentId("doc-1");
      verify(cacheService).invalidateAll();
    }

    @Test
    @DisplayName("Throws when document not found")
    void throwsWhenDocumentNotFound() {
      when(documentRepository.findById("nonexistent")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> documentService.deleteDocument("nonexistent"))
          .isInstanceOf(HrAssistantException.class)
          .satisfies(
              ex ->
                  assertThat(((HrAssistantException) ex).getErrorCode())
                      .isEqualTo(HrAssistantException.ErrorCode.DOCUMENT_NOT_FOUND));
    }
  }

  // ========================================================================
  // renameDocument
  // ========================================================================

  @Nested
  @DisplayName("renameDocument")
  class RenameDocumentTests {

    @Test
    @DisplayName("Renames document in DB and vector store")
    void renamesDocumentSuccessfully() {
      Document doc =
          Document.builder()
              .id("doc-1")
              .filename("old-name.pdf")
              .type(DocumentType.PDF)
              .status(DocumentStatus.INDEXED)
              .build();

      DocumentInfo expectedInfo =
          DocumentInfo.builder().id("doc-1").filename("new-name.pdf").build();

      when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));
      when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
      when(documentMapper.toDocumentInfo(any(Document.class))).thenReturn(expectedInfo);

      DocumentInfo result = documentService.renameDocument("doc-1", "new-name.pdf");

      assertThat(result.getFilename()).isEqualTo("new-name.pdf");
      verify(vectorStoreService).updateDocumentName("doc-1", "new-name.pdf");
    }

    @Test
    @DisplayName("Throws when document not found for rename")
    void throwsWhenDocumentNotFoundForRename() {
      when(documentRepository.findById("missing")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> documentService.renameDocument("missing", "new.pdf"))
          .isInstanceOf(HrAssistantException.class)
          .satisfies(
              ex ->
                  assertThat(((HrAssistantException) ex).getErrorCode())
                      .isEqualTo(HrAssistantException.ErrorCode.DOCUMENT_NOT_FOUND));
    }
  }

  // ========================================================================
  // getAllDocuments / getDocument
  // ========================================================================

  @Nested
  @DisplayName("Query Operations")
  class QueryOperationTests {

    @Test
    @DisplayName("getAllDocuments returns mapped list")
    void getAllDocumentsReturnsMappedList() {
      Document doc1 = Document.builder().id("1").filename("a.pdf").build();
      Document doc2 = Document.builder().id("2").filename("b.txt").build();
      DocumentInfo info1 = DocumentInfo.builder().id("1").filename("a.pdf").build();
      DocumentInfo info2 = DocumentInfo.builder().id("2").filename("b.txt").build();

      when(documentRepository.findAll()).thenReturn(List.of(doc1, doc2));
      when(documentMapper.toDocumentInfo(doc1)).thenReturn(info1);
      when(documentMapper.toDocumentInfo(doc2)).thenReturn(info2);

      List<DocumentInfo> result = documentService.getAllDocuments();

      assertThat(result).hasSize(2);
      assertThat(result).extracting(DocumentInfo::getFilename).containsExactly("a.pdf", "b.txt");
    }

    @Test
    @DisplayName("getDocument returns mapped document info")
    void getDocumentReturnsMappedInfo() {
      Document doc = Document.builder().id("doc-1").filename("policy.pdf").build();
      DocumentInfo info = DocumentInfo.builder().id("doc-1").filename("policy.pdf").build();

      when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));
      when(documentMapper.toDocumentInfo(doc)).thenReturn(info);

      DocumentInfo result = documentService.getDocument("doc-1");

      assertThat(result.getId()).isEqualTo("doc-1");
    }

    @Test
    @DisplayName("getDocument throws when not found")
    void getDocumentThrowsWhenNotFound() {
      when(documentRepository.findById("missing")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> documentService.getDocument("missing"))
          .isInstanceOf(HrAssistantException.class)
          .satisfies(
              ex ->
                  assertThat(((HrAssistantException) ex).getErrorCode())
                      .isEqualTo(HrAssistantException.ErrorCode.DOCUMENT_NOT_FOUND));
    }
  }
}
