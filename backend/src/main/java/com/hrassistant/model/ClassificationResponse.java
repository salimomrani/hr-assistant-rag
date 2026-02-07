package com.hrassistant.model;

/**
 * Structure expected from the LLM classification response. Parsed via {@link
 * org.springframework.ai.converter.BeanOutputConverter}.
 *
 * @param hrRelated LLM's determination of HR relevance
 * @param category category name (mapped to {@link HrCategory} enum)
 * @param confidence confidence level (mapped to {@link ConfidenceLevel} enum)
 */
public record ClassificationResponse(boolean hrRelated, String category, String confidence) {}
