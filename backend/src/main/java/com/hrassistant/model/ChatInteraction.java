package com.hrassistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "chat_interactions",
    indexes = {
      @Index(name = "idx_chat_interaction_created_at", columnList = "created_at"),
      @Index(name = "idx_chat_interaction_question", columnList = "question")
    })
public class ChatInteraction {

  @Id private String id;

  @NotNull
  @Size(max = 1000)
  @Column(nullable = false, length = 1000)
  private String question;

  @NotNull
  @Column(name = "response_time_ms", nullable = false)
  private Long responseTimeMs;

  @ElementCollection
  @CollectionTable(
      name = "chat_interaction_documents",
      joinColumns = @JoinColumn(name = "chat_interaction_id"))
  @Column(name = "document_id")
  @Builder.Default
  private List<String> referencedDocumentIds = new ArrayList<>();

  @NotNull
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      id = UUID.randomUUID().toString();
    }
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }
}
