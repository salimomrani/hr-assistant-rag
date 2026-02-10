# Implementation Plan: Analytics Dashboard

**Branch**: `004-analytics-dashboard` | **Date**: 2026-02-10 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/004-analytics-dashboard/spec.md`

## Summary

Add an analytics dashboard to the HR Assistant RAG application. The backend will persist chat interactions (question, response time, referenced documents) in PostgreSQL via a new `ChatInteraction` JPA entity, and expose REST endpoints for aggregated analytics. The frontend will display a dashboard page with KPI cards, ranked lists (popular questions, top documents), and time-series charts (daily usage, response times) using PrimeNG Chart components backed by Chart.js.

## Technical Context

**Language/Version**: Java 21 (Corretto) + TypeScript 5.9
**Primary Dependencies**: Spring Boot 4.0.1, Spring AI 2.0.0-M1, Spring Data JPA, Angular 21, PrimeNG v21, Chart.js 4.x
**Storage**: PostgreSQL 16 + pgvector (existing), new `chat_interactions` table
**Testing**: JUnit 5 + Mockito (backend), Vitest (frontend)
**Target Platform**: Web (macOS dev, Linux prod)
**Project Type**: Web application (backend + frontend)
**Performance Goals**: Dashboard KPIs load within 2 seconds, server-side aggregation for all analytics queries
**Constraints**: No additional infrastructure (reuse existing PostgreSQL), minimal impact on chat response latency
**Scale/Scope**: Single new page, ~5 new backend files, ~8 new frontend files

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Library-First | PASS | Using PrimeNG Chart (Chart.js wrapper), Spring Data JPA for aggregation queries, no custom implementations |
| II. English-Only Code Artifacts | PASS | All code, comments, and docs in English |
| III. Incremental Validation | PASS | User stories are prioritized P1→P3, each independently testable |
| IV. Artifact Synchronization | PASS | Will maintain tasks.md sync during implementation |
| V. Lean Configuration | PASS | Only adding `chart.js` dependency, no new infrastructure |

No violations. All principles satisfied.

## Project Structure

### Documentation (this feature)

```text
specs/004-analytics-dashboard/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/hrassistant/
│   ├── model/
│   │   └── ChatInteraction.java           # New JPA entity
│   ├── repository/
│   │   └── ChatInteractionRepository.java # New repository with aggregation queries
│   ├── service/
│   │   ├── AnalyticsService.java          # New analytics aggregation service
│   │   └── StreamingRagService.java       # Modified: persist interaction after completion
│   └── controller/
│       └── AnalyticsController.java       # New REST controller
└── src/test/java/com/hrassistant/
    ├── service/
    │   └── AnalyticsServiceTest.java      # New unit tests
    └── controller/
        └── AnalyticsControllerTest.java   # New controller tests

frontend/
├── src/app/
│   ├── features/analytics/
│   │   └── components/
│   │       ├── analytics-page/            # Container component (lazy-loaded)
│   │       ├── kpi-cards/                 # KPI summary cards
│   │       ├── popular-questions/         # Top 10 questions list
│   │       ├── top-documents/             # Top 10 documents list
│   │       └── usage-chart/              # Time-series charts (usage + response time)
│   ├── core/services/
│   │   └── analytics.service.ts           # New analytics API service
│   ├── core/models/
│   │   └── analytics.model.ts             # New analytics DTOs
│   ├── app.routes.ts                      # Modified: add /analytics route
│   └── layout/header/
│       └── header.component.html          # Modified: add Analytics nav link
└── src/app/features/analytics/
    └── components/*/
        └── *.spec.ts                      # Component tests
```

**Structure Decision**: Follows existing web application structure. Analytics feature module mirrors the admin feature pattern (container + presentational components). Backend follows the existing layered architecture (controller → service → repository).
