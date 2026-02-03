package com.hrassistant.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for document metadata exposed in the API.
 *
 * <pre>
 * {
 *   "id": "abc-123",
 *   "filename": "conges.pdf",
 *   "type": "PDF",
 *   "status": "INDEXED",
 *   "size": 45000,
 *   "chunkCount": 12,
 *   "uploadedAt": "2026-01-21T17:30:00",
 *   "indexedAt": "2026-01-21T17:30:05"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentInfo {
    private String id;
    private String filename;
    private DocumentType type;
    private DocumentStatus status;
    private Long size;
    private Integer chunkCount;
    private LocalDateTime uploadedAt;
    private LocalDateTime indexedAt;
    private String errorMessage;
    private String category;
    private Boolean hasFile;
}
