package com.hrassistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for chat responses.
 *
 * <pre>
 * {
 *   "answer": "Vous avez droit à 25 jours de congés payés par an.",
 *   "sources": ["conges.pdf", "reglement.pdf"],
 *   "conversationId": "abc-123"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String answer;
    private List<String> sources;
    private String conversationId;
}
