package com.hrassistant.service;

import com.hrassistant.exception.HrAssistantException;
import com.hrassistant.mapper.DocumentMapper;
import com.hrassistant.model.*;
import com.hrassistant.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final CacheService cacheService;

    @Value("${hr-assistant.documents.max-size-mb:10}")
    private int maxSizeMb;

    @Value("${hr-assistant.rag.chunk-size:500}")
    private int chunkSize;

    @Value("${hr-assistant.rag.chunk-overlap:50}")
    private int chunkOverlap;

    /**
     * Uploads and indexes a document.
     *
     * Pipeline:
     * 1. Validate file (type, size)
     * 2. Extract text (PDF or TXT)
     * 3. Chunk text
     * 4. Store in VectorStore (embedding generated automatically by Spring AI)
     * 5. Save metadata to PostgreSQL
     */
    @Transactional
    public DocumentInfo uploadDocument(MultipartFile file, String category) {
        log.info("Starting document upload: {} (category: {})", file.getOriginalFilename(), category);

        // Step 1: Validate file
        validateFile(file);

        // Create document entity
        String documentId = UUID.randomUUID().toString();
        com.hrassistant.model.Document document = com.hrassistant.model.Document.builder()
                .id(documentId)
                .filename(file.getOriginalFilename())
                .type(DocumentType.fromExtension(file.getOriginalFilename()))
                .status(DocumentStatus.PENDING)
                .size(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .category(category)
                .build();

        // Save to PostgreSQL
        documentRepository.save(document);

        try {
            // Step 2: Extract text
            String text = extractText(file, document.getType());

            // Step 3: Chunk text
            List<DocumentChunk> chunks = chunkText(text, documentId, document.getFilename());

            // Step 4: Store chunks in VectorStore (embedding is automatic)
            indexChunks(chunks);

            // Step 5: Update document status
            document.setStatus(DocumentStatus.INDEXED);
            document.setChunkCount(chunks.size());
            document.setIndexedAt(LocalDateTime.now());
            documentRepository.save(document);

            // Step 6: Invalidate cache (new documents may change answers)
            cacheService.invalidateAll();

            log.info("Document indexed successfully: {} ({} chunks)",
                    document.getFilename(), chunks.size());

            return documentMapper.toDocumentInfo(document);

        } catch (Exception e) {
            log.error("Failed to index document: {}", e.getMessage(), e);
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.DOCUMENT_PROCESSING_ERROR,
                    "Failed to process document: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Validates file type and size.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.INVALID_INPUT,
                    "File is empty"
            );
        }

        // Validate file type
        String filename = file.getOriginalFilename();
        DocumentType type = DocumentType.fromExtension(filename);

        if (type == null) {
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.INVALID_INPUT,
                    "Unsupported file type. Accepted formats: PDF, TXT"
            );
        }

        // Validate file size
        long maxSizeBytes = maxSizeMb * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.INVALID_INPUT,
                    String.format("File exceeds maximum size of %d MB", maxSizeMb)
            );
        }

        log.debug("File validated: {} ({} bytes)", filename, file.getSize());
    }

    /**
     * Extracts text from PDF or TXT file.
     */
    private String extractText(MultipartFile file, DocumentType type) throws IOException {
        log.debug("Extracting text from {} file", type);

        return switch (type) {
            case PDF -> extractTextFromPdf(file);
            case TXT -> extractTextFromTxt(file);
        };
    }

    /**
     * Extracts text from PDF using PDFBox.
     */
    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.isBlank()) {
                throw new HrAssistantException(
                        HrAssistantException.ErrorCode.DOCUMENT_PROCESSING_ERROR,
                        "PDF does not contain extractable text"
                );
            }

            log.debug("Extracted {} characters from PDF", text.length());
            return text;

        } catch (IOException e) {
            log.error("Failed to parse PDF: {}", e.getMessage());
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.DOCUMENT_PROCESSING_ERROR,
                    "PDF file is corrupted or unreadable",
                    e
            );
        }
    }

    /**
     * Extracts text from TXT file.
     */
    private String extractTextFromTxt(MultipartFile file) throws IOException {
        try {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);

            if (text.isBlank()) {
                throw new HrAssistantException(
                        HrAssistantException.ErrorCode.DOCUMENT_PROCESSING_ERROR,
                        "TXT file is empty"
                );
            }

            log.debug("Read {} characters from TXT", text.length());
            return text;

        } catch (IOException e) {
            log.error("Failed to read TXT file: {}", e.getMessage());
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.DOCUMENT_PROCESSING_ERROR,
                    "Unable to read TXT file",
                    e
            );
        }
    }

    /**
     * Chunks text with overlap.
     */
    private List<DocumentChunk> chunkText(String text, String documentId, String documentName) {
        log.debug("Chunking text: {} chars, chunkSize={}, overlap={}",
                text.length(), chunkSize, chunkOverlap);

        List<DocumentChunk> chunks = new ArrayList<>();
        int index = 0;
        int position = 0;

        while (position < text.length()) {
            int end = Math.min(position + chunkSize, text.length());
            String chunkContent = text.substring(position, end);

            DocumentChunk chunk = DocumentChunk.builder()
                    .id(UUID.randomUUID().toString())
                    .documentId(documentId)
                    .documentName(documentName)
                    .index(index++)
                    .content(chunkContent)
                    .build();

            chunks.add(chunk);

            // Move position forward, accounting for overlap
            position += (chunkSize - chunkOverlap);
        }

        log.debug("Created {} chunks", chunks.size());
        return chunks;
    }

    /**
     * Stores chunks in VectorStore.
     * Spring AI automatically generates embeddings when storing.
     */
    private void indexChunks(List<DocumentChunk> chunks) {
        log.debug("Indexing {} chunks", chunks.size());

        chunks.stream()
                .map(embeddingService::toDocument)
                .forEach(vectorStoreService::store);

        log.debug("All chunks indexed successfully");
    }

    /**
     * Retrieves all documents from PostgreSQL.
     */
    public List<DocumentInfo> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(documentMapper::toDocumentInfo)
                .toList();
    }

    /**
     * Retrieves all distinct categories.
     */
    public List<String> getAllCategories() {
        return documentRepository.findDistinctCategories();
    }

    /**
     * Retrieves a document by ID from PostgreSQL.
     */
    public DocumentInfo getDocument(String id) {
        com.hrassistant.model.Document document = documentRepository.findById(id)
                .orElseThrow(() -> new HrAssistantException(
                        HrAssistantException.ErrorCode.DOCUMENT_NOT_FOUND,
                        "Document not found: " + id
                ));
        return documentMapper.toDocumentInfo(document);
    }

    /**
     * Renames a document in the database.
     * Also updates the document name in VectorStore metadata.
     *
     * @param id The document ID
     * @param newFilename The new filename
     * @return Updated DocumentInfo
     */
    @Transactional
    public DocumentInfo renameDocument(String id, String newFilename) {
        com.hrassistant.model.Document document = documentRepository.findById(id)
                .orElseThrow(() -> new HrAssistantException(
                        HrAssistantException.ErrorCode.DOCUMENT_NOT_FOUND,
                        "Document not found: " + id
                ));

        String oldFilename = document.getFilename();

        // Update filename in PostgreSQL
        document.setFilename(newFilename);
        documentRepository.save(document);

        // Update document name in VectorStore metadata
        vectorStoreService.updateDocumentName(id, newFilename);

        log.info("Document renamed: {} -> {}", oldFilename, newFilename);

        return documentMapper.toDocumentInfo(document);
    }

    /**
     * Deletes a document and removes all associated embeddings from VectorStore.
     */
    @Transactional
    public void deleteDocument(String id) {
        com.hrassistant.model.Document document = documentRepository.findById(id)
                .orElseThrow(() -> new HrAssistantException(
                        HrAssistantException.ErrorCode.DOCUMENT_NOT_FOUND,
                        "Document not found: " + id
                ));

        // Remove from PostgreSQL
        documentRepository.delete(document);

        // Remove embeddings from VectorStore
        vectorStoreService.removeByDocumentId(id);

        // Invalidate cache (removed documents may change answers)
        cacheService.invalidateAll();

        log.info("Document deleted: {} (metadata and embeddings removed)", document.getFilename());
    }
}
