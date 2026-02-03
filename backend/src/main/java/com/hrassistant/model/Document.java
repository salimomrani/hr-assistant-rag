package com.hrassistant.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documents")
public class Document {

    @Id
    private String id;

    @Column(nullable = false)
    private String filename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    private long size;

    private int chunkCount;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "indexed_at")
    private LocalDateTime indexedAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "category")
    private String category;

    @Column(name = "file_path")
    private String filePath;
}
