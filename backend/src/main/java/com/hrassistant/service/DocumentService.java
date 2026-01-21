package com.hrassistant.service;

import com.hrassistant.exception.HrAssistantException;
import com.hrassistant.mapper.DocumentMapper;
import com.hrassistant.model.*;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final DocumentMapper documentMapper;

    // In-memory storage for document metadata
    private final Map<String, Document> documents = new ConcurrentHashMap<>();

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
     * 4. Generate embeddings
     * 5. Store in VectorStore
     * 6. Save metadata
     */
    public DocumentInfo uploadDocument(MultipartFile file) {
        log.info("Starting document upload: {}", file.getOriginalFilename());

        // Step 1: Validate file
        validateFile(file);

        // Create document entity
        String documentId = UUID.randomUUID().toString();
        Document document = Document.builder()
                .id(documentId)
                .filename(file.getOriginalFilename())
                .type(DocumentType.fromExtension(file.getOriginalFilename()))
                .status(DocumentStatus.PENDING)
                .size(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        documents.put(documentId, document);

        try {
            // Step 2: Extract text
            String text = extractText(file, document.getType());

            // Step 3: Chunk text
            List<DocumentChunk> chunks = chunkText(text, documentId, document.getFilename());

            // Step 4 & 5: Embed and store chunks
            indexChunks(chunks);

            // Step 6: Update document status
            document.setStatus(DocumentStatus.INDEXED);
            document.setChunkCount(chunks.size());
            document.setIndexedAt(LocalDateTime.now());

            log.info("Document indexed successfully: {} ({} chunks)",
                    document.getFilename(), chunks.size());

            return documentMapper.toDocumentInfo(document);

        } catch (Exception e) {
            log.error("Failed to index document: {}", e.getMessage(), e);
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(e.getMessage());
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
                    "Le fichier est vide"
            );
        }

        // Validate file type
        String filename = file.getOriginalFilename();
        DocumentType type = DocumentType.fromExtension(filename);

        if (type == null) {
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.INVALID_INPUT,
                    "Type de fichier non supporté. Formats acceptés: PDF, TXT"
            );
        }

        // Validate file size
        long maxSizeBytes = maxSizeMb * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.INVALID_INPUT,
                    String.format("Le fichier dépasse la taille maximale de %d MB", maxSizeMb)
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
                        "Le PDF ne contient pas de texte extractible"
                );
            }

            log.debug("Extracted {} characters from PDF", text.length());
            return text;

        } catch (IOException e) {
            log.error("Failed to parse PDF: {}", e.getMessage());
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.DOCUMENT_PROCESSING_ERROR,
                    "Le fichier PDF est corrompu ou illisible",
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
                        "Le fichier TXT est vide"
                );
            }

            log.debug("Read {} characters from TXT", text.length());
            return text;

        } catch (IOException e) {
            log.error("Failed to read TXT file: {}", e.getMessage());
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.DOCUMENT_PROCESSING_ERROR,
                    "Impossible de lire le fichier TXT",
                    e
            );
        }
    }

    /**
     * Chunks text with overlap.
     *
     * Example: chunkSize=500, overlap=50
     * Chunk 1: chars 0-500
     * Chunk 2: chars 450-950 (starts 50 chars before end of chunk 1)
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
     * Generates embeddings for chunks and stores them.
     */
    private void indexChunks(List<DocumentChunk> chunks) {
        log.debug("Indexing {} chunks", chunks.size());

        for (DocumentChunk chunk : chunks) {
            // Convert to TextSegment
            TextSegment segment = embeddingService.toTextSegment(chunk);

            // Generate embedding
            Embedding embedding = embeddingService.embed(chunk.getContent());

            // Store in vector store
            vectorStoreService.store(embedding, segment);
        }

        log.debug("All chunks indexed successfully");
    }

    /**
     * Retrieves all documents.
     */
    public List<DocumentInfo> getAllDocuments() {
        return documents.values().stream()
                .map(documentMapper::toDocumentInfo)
                .toList();
    }

    /**
     * Retrieves a document by ID.
     */
    public DocumentInfo getDocument(String id) {
        Document document = documents.get(id);
        if (document == null) {
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.DOCUMENT_NOT_FOUND,
                    "Document not found: " + id
            );
        }
        return documentMapper.toDocumentInfo(document);
    }

    /**
     * Deletes a document and removes all associated embeddings from VectorStore.
     */
    public void deleteDocument(String id) {
        Document document = documents.remove(id);
        if (document == null) {
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.DOCUMENT_NOT_FOUND,
                    "Document not found: " + id
            );
        }

        // Remove embeddings from VectorStore
        vectorStoreService.removeByDocumentId(id);

        log.info("Document deleted: {} (metadata and embeddings removed)", document.getFilename());
    }
}
