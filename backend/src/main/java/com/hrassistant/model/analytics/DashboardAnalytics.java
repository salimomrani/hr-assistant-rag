package com.hrassistant.model.analytics;

import java.util.List;

public record DashboardAnalytics(
    long totalQuestionsToday,
    double averageResponseTimeMs,
    long totalDocuments,
    long conversationsThisWeek,
    List<QuestionFrequency> popularQuestions,
    List<DocumentReference> topDocuments,
    List<DailyCount> dailyUsage,
    List<DailyAverage> dailyResponseTimes) {}
