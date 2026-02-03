package com.hrassistant.controller;

import com.hrassistant.model.DocumentInfo;
import com.hrassistant.model.RenameRequest;
import com.hrassistant.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

/**
 * REST controller for document management.
 *
 * Endpoints:
 * - POST /api/documents - Upload and index a document
 * - GET /api/documents - List all indexed documents
 * - GET /api/documents/{id} - Get document by ID
 * - PATCH /api/documents/{id} - Rename a document
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
     * @param category Optional category for organizing documents
     * @return DocumentInfo with upload status and metadata
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentInfo> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category) {
        log.info("Received document upload request: {} (category: {})", file.getOriginalFilename(), category);

        DocumentInfo documentInfo = documentService.uploadDocument(file, category);

        log.info("Document uploaded successfully: {} (id: {})",
                documentInfo.getFilename(), documentInfo.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(documentInfo);
    }

    /**
     * Lists all distinct categories.
     *
     * @return List of category names
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("Received request to list all categories");
        List<String> categories = documentService.getAllCategories();
        return ResponseEntity.ok(categories);
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
     * Downloads/views the original document file.
     *
     * @param id The document ID
     * @return The file content with appropriate Content-Type
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getDocumentFile(@PathVariable String id) {
        log.info("Received request to get document file: {}", id);

        Path filePath = documentService.getDocumentFilePath(id);
        if (filePath == null) {
            log.warn("Document file not found: {}", id);
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("Document file not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // Determine content type based on file extension
            String contentType = filePath.toString().toLowerCase().endsWith(".pdf")
                    ? "application/pdf"
                    : "text/plain; charset=utf-8";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("Failed to create resource URL: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Renames a document.
     *
     * Updates the filename in the database and in VectorStore metadata.
     *
     * @param id The document ID
     * @param request The rename request containing the new filename
     * @return Updated DocumentInfo
     */
    @PatchMapping("/{id}")
    public ResponseEntity<DocumentInfo> renameDocument(
            @PathVariable String id,
            @Valid @RequestBody RenameRequest request) {
        log.info("Received request to rename document: {} -> {}", id, request.getNewFilename());

        DocumentInfo documentInfo = documentService.renameDocument(id, request.getNewFilename());

        log.info("Document renamed successfully: {}", documentInfo.getFilename());

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
