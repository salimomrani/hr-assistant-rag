package com.hrassistant.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
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
 *   "conversationId": "abc-123",
 *   "documentIds": ["doc-1", "doc-2"]
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

  @NotBlank(message = "Question cannot be empty")
  @Size(max = 1000, message = "Question cannot exceed 1000 characters")
  private String question;

  @Size(max = 100, message = "Conversation ID cannot exceed 100 characters")
  private String conversationId;

  /**
   * Optional list of document IDs to filter the RAG search. If null or empty, all indexed documents
   * will be searched.
   */
  private List<String> documentIds;
}
