package com.hrassistant.service;

import com.hrassistant.model.DocumentChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    /**
     * Converts a DocumentChunk to a Spring AI Document.
     * The Document class handles both content and metadata.
     */
    public Document toDocument(DocumentChunk chunk) {
        Map<String, Object> metadata = Map.of(
                "documentId", chunk.getDocumentId(),
                "documentName", chunk.getDocumentName(),
                "chunkIndex", chunk.getIndex()
        );

        return new Document(chunk.getId(), chunk.getContent(), metadata);
    }
}
