package com.hrassistant.service;

import com.hrassistant.model.analytics.*;
import com.hrassistant.repository.ChatInteractionRepository;
import com.hrassistant.repository.DocumentRepository;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

  private final ChatInteractionRepository chatInteractionRepository;
  private final DocumentRepository documentRepository;

  /** Assembles the full dashboard analytics by querying all repository methods. */
  public DashboardAnalytics getDashboardAnalytics() {
    log.debug("Building dashboard analytics");

    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
    LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

    long totalQuestionsToday = chatInteractionRepository.countByCreatedAtAfter(startOfToday);
    double averageResponseTimeMs = chatInteractionRepository.findAverageResponseTimeMs();
    long totalDocuments = documentRepository.count();
    long conversationsThisWeek =
        chatInteractionRepository.countByCreatedAtBetween(startOfWeek, LocalDateTime.now());

    List<QuestionFrequency> popularQuestions =
        mapQuestionFrequencies(chatInteractionRepository.findTopQuestionsByFrequency());

    List<DocumentReference> topDocuments =
        mapDocumentReferences(chatInteractionRepository.findTopReferencedDocuments());

    List<DailyCount> dailyUsage =
        mapDailyCounts(chatInteractionRepository.countDailyInteractions(thirtyDaysAgo));

    List<DailyAverage> dailyResponseTimes =
        mapDailyAverages(chatInteractionRepository.findDailyAverageResponseTimes(thirtyDaysAgo));

    log.info(
        "Dashboard analytics built: {} questions today, {} docs, {} this week",
        totalQuestionsToday,
        totalDocuments,
        conversationsThisWeek);

    return DashboardAnalytics.builder()
        .totalQuestionsToday(totalQuestionsToday)
        .averageResponseTimeMs(averageResponseTimeMs)
        .totalDocuments(totalDocuments)
        .conversationsThisWeek(conversationsThisWeek)
        .popularQuestions(popularQuestions)
        .topDocuments(topDocuments)
        .dailyUsage(dailyUsage)
        .dailyResponseTimes(dailyResponseTimes)
        .build();
  }

  private List<QuestionFrequency> mapQuestionFrequencies(List<Object[]> rows) {
    return rows.stream()
        .map(row -> new QuestionFrequency((String) row[0], ((Number) row[1]).longValue()))
        .toList();
  }

  private List<DocumentReference> mapDocumentReferences(List<Object[]> rows) {
    return rows.stream()
        .map(
            row ->
                DocumentReference.builder()
                    .documentId((String) row[0])
                    .documentName((String) row[1])
                    .referenceCount(((Number) row[2]).longValue())
                    .build())
        .toList();
  }

  private List<DailyCount> mapDailyCounts(List<Object[]> rows) {
    return rows.stream()
        .map(row -> new DailyCount(toLocalDate(row[0]), ((Number) row[1]).longValue()))
        .toList();
  }

  private List<DailyAverage> mapDailyAverages(List<Object[]> rows) {
    return rows.stream()
        .map(row -> new DailyAverage(toLocalDate(row[0]), ((Number) row[1]).doubleValue()))
        .toList();
  }

  private LocalDate toLocalDate(Object value) {
    if (value instanceof Date sqlDate) {
      return sqlDate.toLocalDate();
    }
    if (value instanceof LocalDate ld) {
      return ld;
    }
    if (value instanceof LocalDateTime ldt) {
      return ldt.toLocalDate();
    }
    return LocalDate.parse(value.toString());
  }
}
