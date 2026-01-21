package com.hrassistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author : salimomrani
 * @email : omrani_salim@outlook.fr
 * @created : 21/01/2026, mercredi
 **/
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
