package com.hrassistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    private String id;
    private String filename;
    private DocumentType type;
    private DocumentStatus status;
    private long size;
    private int chunkCount;
    private LocalDateTime uploadedAt;
    private LocalDateTime indexedAt;
    private String errorMessage;
}
