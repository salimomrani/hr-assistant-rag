package com.hrassistant.repository;

import com.hrassistant.model.ChatInteraction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatInteractionRepository extends JpaRepository<ChatInteraction, String> {

  long countByCreatedAtAfter(LocalDateTime after);

  @Query("SELECT COALESCE(AVG(ci.responseTimeMs), 0) FROM ChatInteraction ci")
  double findAverageResponseTimeMs();

  long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

  /** Finds the top 10 most frequently asked questions, grouped by lowercase question text. */
  @Query(
      value =
          """
          SELECT LOWER(ci.question) AS question, COUNT(*) AS cnt
          FROM chat_interactions ci
          GROUP BY LOWER(ci.question)
          ORDER BY cnt DESC
          LIMIT 10
          """,
      nativeQuery = true)
  List<Object[]> findTopQuestionsByFrequency();

  /** Counts daily interactions for the last 30 days. */
  @Query(
      value =
          """
          SELECT CAST(ci.created_at AS DATE) AS day, COUNT(*) AS cnt
          FROM chat_interactions ci
          WHERE ci.created_at >= :since
          GROUP BY CAST(ci.created_at AS DATE)
          ORDER BY day
          """,
      nativeQuery = true)
  List<Object[]> countDailyInteractions(@Param("since") LocalDateTime since);

  /** Finds daily average response times for the last 30 days. */
  @Query(
      value =
          """
          SELECT CAST(ci.created_at AS DATE) AS day, AVG(ci.response_time_ms) AS avg_ms
          FROM chat_interactions ci
          WHERE ci.created_at >= :since
          GROUP BY CAST(ci.created_at AS DATE)
          ORDER BY day
          """,
      nativeQuery = true)
  List<Object[]> findDailyAverageResponseTimes(@Param("since") LocalDateTime since);

  /**
   * Finds top 10 most referenced documents by joining the chat_interaction_documents collection
   * table with the documents table. Excludes deleted documents.
   */
  @Query(
      value =
          """
          SELECT cid.document_id, d.filename, COUNT(*) AS ref_count
          FROM chat_interaction_documents cid
          JOIN documents d ON d.id = cid.document_id
          GROUP BY cid.document_id, d.filename
          ORDER BY ref_count DESC
          LIMIT 10
          """,
      nativeQuery = true)
  List<Object[]> findTopReferencedDocuments();
}
