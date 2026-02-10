package com.hrassistant.controller;

import com.hrassistant.model.analytics.DashboardAnalytics;
import com.hrassistant.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  @GetMapping("/dashboard")
  public ResponseEntity<DashboardAnalytics> getDashboardAnalytics() {
    log.info("Received request for dashboard analytics");
    DashboardAnalytics analytics = analyticsService.getDashboardAnalytics();
    return ResponseEntity.ok(analytics);
  }
}
