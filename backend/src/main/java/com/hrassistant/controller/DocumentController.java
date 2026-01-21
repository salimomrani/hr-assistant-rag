package com.hrassistant.controller;

import com.hrassistant.model.DocumentInfo;
import com.hrassistant.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for document management.
 *
 * Endpoints:
 * - POST /api/documents - Upload and index a document
 * - GET /api/documents - List all indexed documents
 * - GET /api/documents/{id} - Get document by ID
 * - DELETE /api/documents/{id} - Delete a document
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Uploads and indexes a document (PDF or TXT).
     *
     * Pipeline:
     * 1. Validate file (type, size)
     * 2. Extract text
     * 3. Chunk text
     * 4. Generate embeddings
     * 5. Store in VectorStore
     * 6. Return metadata
     *
     * @param file The document to upload (multipart/form-data)
     * @return DocumentInfo with upload status and metadata
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentInfo> uploadDocument(@RequestParam("file") MultipartFile file) {
        log.info("Received document upload request: {}", file.getOriginalFilename());

        DocumentInfo documentInfo = documentService.uploadDocument(file);

        log.info("Document uploaded successfully: {} (id: {})",
                documentInfo.getFilename(), documentInfo.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(documentInfo);
    }

    /**
     * Lists all indexed documents.
     *
     * @return List of DocumentInfo with metadata
     */
    @GetMapping
    public ResponseEntity<List<DocumentInfo>> getAllDocuments() {
        log.info("Received request to list all documents");

        List<DocumentInfo> documents = documentService.getAllDocuments();

        log.info("Returning {} documents", documents.size());

        return ResponseEntity.ok(documents);
    }

    /**
     * Retrieves a document by ID.
     *
     * @param id The document ID
     * @return DocumentInfo with metadata
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentInfo> getDocument(@PathVariable String id) {
        log.info("Received request to get document: {}", id);

        DocumentInfo documentInfo = documentService.getDocument(id);

        return ResponseEntity.ok(documentInfo);
    }

    /**
     * Deletes a document by ID.
     *
     * Note: This only removes metadata. Chunks remain in VectorStore for now.
     *
     * @param id The document ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        log.info("Received request to delete document: {}", id);

        documentService.deleteDocument(id);

        log.info("Document deleted: {}", id);

        return ResponseEntity.noContent().build();
    }
}
