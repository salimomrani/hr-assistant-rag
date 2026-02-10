package com.hrassistant.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.hrassistant.model.analytics.*;
import com.hrassistant.service.AnalyticsService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

  @Mock private AnalyticsService analyticsService;

  @InjectMocks private AnalyticsController analyticsController;

  @Test
  @DisplayName("GET /api/analytics/dashboard returns 200 with correct data")
  void getDashboardReturns200WithCorrectData() {
    DashboardAnalytics analytics =
        DashboardAnalytics.builder()
            .totalQuestionsToday(42L)
            .averageResponseTimeMs(1500.0)
            .totalDocuments(10L)
            .conversationsThisWeek(120L)
            .popularQuestions(List.of(new QuestionFrequency("leave policy", 15)))
            .topDocuments(
                List.of(
                    DocumentReference.builder()
                        .documentId("doc-1")
                        .documentName("handbook.pdf")
                        .referenceCount(25)
                        .build()))
            .dailyUsage(List.of(new DailyCount(LocalDate.of(2026, 2, 8), 5)))
            .dailyResponseTimes(List.of(new DailyAverage(LocalDate.of(2026, 2, 8), 1200.5)))
            .build();

    when(analyticsService.getDashboardAnalytics()).thenReturn(analytics);

    ResponseEntity<DashboardAnalytics> response = analyticsController.getDashboardAnalytics();

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().totalQuestionsToday()).isEqualTo(42L);
    assertThat(response.getBody().averageResponseTimeMs()).isEqualTo(1500.0);
    assertThat(response.getBody().totalDocuments()).isEqualTo(10L);
    assertThat(response.getBody().conversationsThisWeek()).isEqualTo(120L);
    assertThat(response.getBody().popularQuestions()).hasSize(1);
    assertThat(response.getBody().popularQuestions().getFirst().question())
        .isEqualTo("leave policy");
    assertThat(response.getBody().topDocuments()).hasSize(1);
    assertThat(response.getBody().topDocuments().getFirst().documentName())
        .isEqualTo("handbook.pdf");
    assertThat(response.getBody().dailyUsage()).hasSize(1);
    assertThat(response.getBody().dailyResponseTimes()).hasSize(1);
  }

  @Test
  @DisplayName("GET /api/analytics/dashboard returns empty analytics when no data")
  void getDashboardReturnsEmptyAnalytics() {
    DashboardAnalytics emptyAnalytics =
        DashboardAnalytics.builder()
            .totalQuestionsToday(0L)
            .averageResponseTimeMs(0.0)
            .totalDocuments(0L)
            .conversationsThisWeek(0L)
            .popularQuestions(List.of())
            .topDocuments(List.of())
            .dailyUsage(List.of())
            .dailyResponseTimes(List.of())
            .build();

    when(analyticsService.getDashboardAnalytics()).thenReturn(emptyAnalytics);

    ResponseEntity<DashboardAnalytics> response = analyticsController.getDashboardAnalytics();

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().totalQuestionsToday()).isEqualTo(0L);
    assertThat(response.getBody().popularQuestions()).isEmpty();
    assertThat(response.getBody().topDocuments()).isEmpty();
    assertThat(response.getBody().dailyUsage()).isEmpty();
    assertThat(response.getBody().dailyResponseTimes()).isEmpty();
  }
}
