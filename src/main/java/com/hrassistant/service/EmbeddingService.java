package com.hrassistant.service;

import com.hrassistant.exception.HrAssistantException;
import com.hrassistant.model.DocumentChunk;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author : salimomrani
 * @email : omrani_salim@outlook.fr
 * @created : 21/01/2026, mercredi
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public TextSegment toTextSegment(DocumentChunk chunk) {
        Metadata metadata = Metadata.from("documentId", chunk.getDocumentId())
                .put("documentName", chunk.getDocumentName())
                .put("chunkIndex", chunk.getIndex());

        return TextSegment.from(chunk.getContent(), metadata);
    }

    public Embedding embed(String text) {
        try {
            log.debug("Generating embedding for text of length: {}", text.length());
            return embeddingModel.embed(text).content();
        } catch (Exception e) {
            log.error("Failed to generate embedding: {}", e.getMessage(), e);
            throw new HrAssistantException(
                    HrAssistantException.ErrorCode.EMBEDDING_ERROR,
                    "Failed to generate embedding: " + e.getMessage(),
                    e
            );
        }
    }
}
