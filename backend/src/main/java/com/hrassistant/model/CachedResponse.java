package com.hrassistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a cached LLM response for semantic caching.
 * Stores the question embedding for similarity matching and the complete response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The original question text.
     */
    private String question;

    /**
     * The question embedding (768 dimensions for nomic-embed-text).
     * Used for semantic similarity matching.
     */
    private float[] questionEmbedding;

    /**
     * The complete LLM response including sources.
     */
    private String response;

    /**
     * The sources used to generate the response.
     */
    private List<String> sources;

    /**
     * Timestamp when this response was cached.
     */
    private LocalDateTime cachedAt;
}
