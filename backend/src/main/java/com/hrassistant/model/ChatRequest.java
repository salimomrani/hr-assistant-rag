package com.hrassistant.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "La question ne peut pas être vide")
    @Size(max = 1000, message = "La question ne peut pas dépasser 1000 caractères")
    private String question;

    @Size(max = 100, message = "L'identifiant de conversation ne peut pas dépasser 100 caractères")
    private String conversationId;
}
