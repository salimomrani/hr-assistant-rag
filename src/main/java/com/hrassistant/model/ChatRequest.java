package com.hrassistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for chat requests.
 *
 * <pre>
 * POST /api/chat
 * {
 *   "question": "Combien de jours de congés ai-je ?",
 *   "conversationId": "abc-123"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String question;
    private String conversationId;
}
