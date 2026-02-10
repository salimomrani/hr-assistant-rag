# Quickstart: Analytics Dashboard

**Feature**: 004-analytics-dashboard
**Date**: 2026-02-10

## Prerequisites

- Docker Compose running (PostgreSQL + Redis): `cd backend && docker compose up -d`
- Ollama running with models: `ollama serve` (llama3.2 + nomic-embed-text)
- Backend running: `cd backend && mvn spring-boot:run`
- Frontend running: `cd frontend && npm start`

## New Dependency

Install Chart.js for PrimeNG charts:

```bash
cd frontend && npm install chart.js
```

## Implementation Order

### Phase 1: Backend Data Layer (P1 - KPIs)
1. Create `ChatInteraction` JPA entity with `@Entity`, `@Table`, `@ElementCollection`
2. Create `ChatInteractionRepository` with aggregation `@Query` methods
3. Modify `CachingStreamingRagService` to persist interactions after stream completion
4. Create analytics DTO records: `DashboardAnalytics`, `QuestionFrequency`, `DocumentReference`, `DailyCount`, `DailyAverage`
5. Create `AnalyticsService` that calls repository methods and assembles `DashboardAnalytics`
6. Create `AnalyticsController` with `GET /api/analytics/dashboard`

### Phase 2: Frontend Dashboard (P1 - KPIs)
7. Create `analytics.model.ts` with TypeScript interfaces matching backend DTOs
8. Create `analytics.service.ts` with `getDashboardAnalytics$()` method
9. Create `analytics-page` container component (lazy-loaded)
10. Create `kpi-cards` component displaying 4 KPI cards
11. Add route `/analytics` in `app.routes.ts`
12. Add "Analytics" nav link in header

### Phase 3: Lists (P2)
13. Create `popular-questions` component (ranked list with truncation)
14. Create `top-documents` component (ranked list with reference counts)

### Phase 4: Charts (P3)
15. Create `usage-chart` component with daily usage line chart
16. Add response time trend chart to `usage-chart` component

### Phase 5: Tests
17. Backend unit tests: `AnalyticsServiceTest`, `AnalyticsControllerTest`
18. Frontend component tests: all analytics components

## Verification

```bash
# Backend compiles
cd backend && mvn clean compile

# Backend tests pass
cd backend && mvn test

# Frontend compiles
cd frontend && ng build

# Frontend tests pass
cd frontend && ng test

# Manual: navigate to http://localhost:4200/analytics
# Verify KPI cards, popular questions, top documents, charts
```

## API Test

```bash
# After some chat interactions exist:
curl http://localhost:8080/api/analytics/dashboard | jq .
```
