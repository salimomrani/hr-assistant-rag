package com.hrassistant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.hrassistant.model.analytics.*;
import com.hrassistant.repository.ChatInteractionRepository;
import com.hrassistant.repository.DocumentRepository;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

  @Mock private ChatInteractionRepository chatInteractionRepository;
  @Mock private DocumentRepository documentRepository;

  private AnalyticsService analyticsService;

  @BeforeEach
  void setUp() {
    analyticsService = new AnalyticsService(chatInteractionRepository, documentRepository);
  }

  // ========================================================================
  // getDashboardAnalytics — with data
  // ========================================================================

  @Nested
  @DisplayName("getDashboardAnalytics with data")
  class WithDataTests {

    @Test
    @DisplayName("Returns correct KPIs when data exists")
    void returnsCorrectKpis() {
      when(chatInteractionRepository.countByCreatedAtAfter(any())).thenReturn(42L);
      when(chatInteractionRepository.findAverageResponseTimeMs()).thenReturn(1500.0);
      when(documentRepository.count()).thenReturn(10L);
      when(chatInteractionRepository.countByCreatedAtBetween(any(), any())).thenReturn(120L);
      when(chatInteractionRepository.findTopQuestionsByFrequency()).thenReturn(List.of());
      when(chatInteractionRepository.findTopReferencedDocuments()).thenReturn(List.of());
      when(chatInteractionRepository.countDailyInteractions(any())).thenReturn(List.of());
      when(chatInteractionRepository.findDailyAverageResponseTimes(any())).thenReturn(List.of());

      DashboardAnalytics result = analyticsService.getDashboardAnalytics();

      assertThat(result.totalQuestionsToday()).isEqualTo(42L);
      assertThat(result.averageResponseTimeMs()).isEqualTo(1500.0);
      assertThat(result.totalDocuments()).isEqualTo(10L);
      assertThat(result.conversationsThisWeek()).isEqualTo(120L);
    }

    @Test
    @DisplayName("popularQuestions correctly mapped from repository results")
    void popularQuestionsMapped() {
      when(chatInteractionRepository.countByCreatedAtAfter(any())).thenReturn(0L);
      when(chatInteractionRepository.findAverageResponseTimeMs()).thenReturn(0.0);
      when(documentRepository.count()).thenReturn(0L);
      when(chatInteractionRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
      when(chatInteractionRepository.findTopQuestionsByFrequency())
          .thenReturn(
              List.of(
                  new Object[] {"what is the leave policy", 15L},
                  new Object[] {"how to request time off", 8L}));
      when(chatInteractionRepository.findTopReferencedDocuments()).thenReturn(List.of());
      when(chatInteractionRepository.countDailyInteractions(any())).thenReturn(List.of());
      when(chatInteractionRepository.findDailyAverageResponseTimes(any())).thenReturn(List.of());

      DashboardAnalytics result = analyticsService.getDashboardAnalytics();

      assertThat(result.popularQuestions()).hasSize(2);
      assertThat(result.popularQuestions().get(0).question()).isEqualTo("what is the leave policy");
      assertThat(result.popularQuestions().get(0).count()).isEqualTo(15L);
      assertThat(result.popularQuestions().get(1).question()).isEqualTo("how to request time off");
      assertThat(result.popularQuestions().get(1).count()).isEqualTo(8L);
    }

    @Test
    @DisplayName("topDocuments correctly mapped from repository results")
    void topDocumentsMapped() {
      when(chatInteractionRepository.countByCreatedAtAfter(any())).thenReturn(0L);
      when(chatInteractionRepository.findAverageResponseTimeMs()).thenReturn(0.0);
      when(documentRepository.count()).thenReturn(0L);
      when(chatInteractionRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
      when(chatInteractionRepository.findTopQuestionsByFrequency()).thenReturn(List.of());
      when(chatInteractionRepository.findTopReferencedDocuments())
          .thenReturn(
              List.of(
                  new Object[] {"doc-1", "leave-policy.pdf", 25L},
                  new Object[] {"doc-2", "handbook.pdf", 12L}));
      when(chatInteractionRepository.countDailyInteractions(any())).thenReturn(List.of());
      when(chatInteractionRepository.findDailyAverageResponseTimes(any())).thenReturn(List.of());

      DashboardAnalytics result = analyticsService.getDashboardAnalytics();

      assertThat(result.topDocuments()).hasSize(2);
      assertThat(result.topDocuments().get(0).documentId()).isEqualTo("doc-1");
      assertThat(result.topDocuments().get(0).documentName()).isEqualTo("leave-policy.pdf");
      assertThat(result.topDocuments().get(0).referenceCount()).isEqualTo(25L);
      assertThat(result.topDocuments().get(1).documentId()).isEqualTo("doc-2");
      assertThat(result.topDocuments().get(1).documentName()).isEqualTo("handbook.pdf");
      assertThat(result.topDocuments().get(1).referenceCount()).isEqualTo(12L);
    }

    @Test
    @DisplayName("dailyUsage correctly mapped from repository results")
    void dailyUsageMapped() {
      LocalDate day1 = LocalDate.of(2026, 2, 8);
      LocalDate day2 = LocalDate.of(2026, 2, 9);

      when(chatInteractionRepository.countByCreatedAtAfter(any())).thenReturn(0L);
      when(chatInteractionRepository.findAverageResponseTimeMs()).thenReturn(0.0);
      when(documentRepository.count()).thenReturn(0L);
      when(chatInteractionRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
      when(chatInteractionRepository.findTopQuestionsByFrequency()).thenReturn(List.of());
      when(chatInteractionRepository.findTopReferencedDocuments()).thenReturn(List.of());
      when(chatInteractionRepository.countDailyInteractions(any()))
          .thenReturn(
              List.of(
                  new Object[] {Date.valueOf(day1), 5L}, new Object[] {Date.valueOf(day2), 12L}));
      when(chatInteractionRepository.findDailyAverageResponseTimes(any())).thenReturn(List.of());

      DashboardAnalytics result = analyticsService.getDashboardAnalytics();

      assertThat(result.dailyUsage()).hasSize(2);
      assertThat(result.dailyUsage().get(0).date()).isEqualTo(day1);
      assertThat(result.dailyUsage().get(0).count()).isEqualTo(5L);
      assertThat(result.dailyUsage().get(1).date()).isEqualTo(day2);
      assertThat(result.dailyUsage().get(1).count()).isEqualTo(12L);
    }

    @Test
    @DisplayName("dailyResponseTimes correctly mapped from repository results")
    void dailyResponseTimesMapped() {
      LocalDate day1 = LocalDate.of(2026, 2, 8);
      LocalDate day2 = LocalDate.of(2026, 2, 9);

      when(chatInteractionRepository.countByCreatedAtAfter(any())).thenReturn(0L);
      when(chatInteractionRepository.findAverageResponseTimeMs()).thenReturn(0.0);
      when(documentRepository.count()).thenReturn(0L);
      when(chatInteractionRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
      when(chatInteractionRepository.findTopQuestionsByFrequency()).thenReturn(List.of());
      when(chatInteractionRepository.findTopReferencedDocuments()).thenReturn(List.of());
      when(chatInteractionRepository.countDailyInteractions(any())).thenReturn(List.of());
      when(chatInteractionRepository.findDailyAverageResponseTimes(any()))
          .thenReturn(
              List.of(
                  new Object[] {Date.valueOf(day1), 1200.5},
                  new Object[] {Date.valueOf(day2), 980.3}));

      DashboardAnalytics result = analyticsService.getDashboardAnalytics();

      assertThat(result.dailyResponseTimes()).hasSize(2);
      assertThat(result.dailyResponseTimes().get(0).date()).isEqualTo(day1);
      assertThat(result.dailyResponseTimes().get(0).averageMs()).isEqualTo(1200.5);
      assertThat(result.dailyResponseTimes().get(1).date()).isEqualTo(day2);
      assertThat(result.dailyResponseTimes().get(1).averageMs()).isEqualTo(980.3);
    }
  }

  // ========================================================================
  // getDashboardAnalytics — no data
  // ========================================================================

  @Nested
  @DisplayName("getDashboardAnalytics with no data")
  class NoDataTests {

    @Test
    @DisplayName("Returns zeros and empty lists when no data exists")
    void returnsZerosAndEmptyLists() {
      when(chatInteractionRepository.countByCreatedAtAfter(any())).thenReturn(0L);
      when(chatInteractionRepository.findAverageResponseTimeMs()).thenReturn(0.0);
      when(documentRepository.count()).thenReturn(0L);
      when(chatInteractionRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
      when(chatInteractionRepository.findTopQuestionsByFrequency()).thenReturn(List.of());
      when(chatInteractionRepository.findTopReferencedDocuments()).thenReturn(List.of());
      when(chatInteractionRepository.countDailyInteractions(any())).thenReturn(List.of());
      when(chatInteractionRepository.findDailyAverageResponseTimes(any())).thenReturn(List.of());

      DashboardAnalytics result = analyticsService.getDashboardAnalytics();

      assertThat(result.totalQuestionsToday()).isZero();
      assertThat(result.averageResponseTimeMs()).isZero();
      assertThat(result.totalDocuments()).isZero();
      assertThat(result.conversationsThisWeek()).isZero();
      assertThat(result.popularQuestions()).isEmpty();
      assertThat(result.topDocuments()).isEmpty();
      assertThat(result.dailyUsage()).isEmpty();
      assertThat(result.dailyResponseTimes()).isEmpty();
    }
  }
}
