# Tasks: Analytics Dashboard

**Input**: Design documents from `/specs/004-analytics-dashboard/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/analytics-api.yaml, quickstart.md
**Branch**: `004-analytics-dashboard`

**Tech Stack**: Java 21 + Spring Boot 4.0.1 + Spring Data JPA | Angular 21 + PrimeNG v21 + Chart.js 4.x | PostgreSQL 16

**Organization**: Tasks grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

---

## Phase 1: Setup

**Purpose**: Install dependencies and prepare project structure

- [ ] T001 Install Chart.js dependency in `frontend/package.json` via `cd frontend && npm install chart.js`
- [ ] T002 Create analytics feature directory structure: `frontend/src/app/features/analytics/components/`

**Checkpoint**: Dependencies installed, directory structure ready

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Data persistence layer that ALL user stories depend on

**CRITICAL**: No user story work can begin until this phase is complete

- [ ] T003 Create `ChatInteraction` JPA entity in `backend/src/main/java/com/hrassistant/model/ChatInteraction.java` with fields: id (UUID), question (String, max 1000), responseTimeMs (Long), referencedDocumentIds (ElementCollection), createdAt (LocalDateTime). Include `@Table` with indexes on `createdAt` and `LOWER(question)`
- [ ] T004 Create `ChatInteractionRepository` in `backend/src/main/java/com/hrassistant/repository/ChatInteractionRepository.java` extending `JpaRepository<ChatInteraction, String>` with `@Query` methods: `countByCreatedAtAfter()`, `findAverageResponseTimeMs()`, `countByCreatedAtBetween()`, `findTopQuestionsByFrequency()`, `countDailyInteractions()`, `findDailyAverageResponseTimes()`
- [ ] T005 Create analytics DTO records in `backend/src/main/java/com/hrassistant/model/analytics/`: `DashboardAnalytics`, `QuestionFrequency`, `DocumentReference`, `DailyCount`, `DailyAverage` as Java records matching contracts/analytics-api.yaml schemas
- [ ] T006 Modify `CachingStreamingRagService` in `backend/src/main/java/com/hrassistant/service/CachingStreamingRagService.java` to persist `ChatInteraction` asynchronously after stream completion using `doOnComplete` + `Schedulers.boundedElastic()`. Capture start time, collect referenced document IDs from sources, and save via `ChatInteractionRepository`
- [ ] T007 Create `AnalyticsService` in `backend/src/main/java/com/hrassistant/service/AnalyticsService.java` that assembles `DashboardAnalytics` by calling `ChatInteractionRepository` aggregation queries and `DocumentRepository.count()`
- [ ] T008 Create `AnalyticsController` in `backend/src/main/java/com/hrassistant/controller/AnalyticsController.java` with `GET /api/analytics/dashboard` returning `ResponseEntity<DashboardAnalytics>` via `AnalyticsService`
- [ ] T009 [P] Create `analytics.model.ts` in `frontend/src/app/core/models/analytics.model.ts` with TypeScript interfaces: `DashboardAnalytics`, `QuestionFrequency`, `DocumentReference`, `DailyCount`, `DailyAverage` matching backend DTOs
- [ ] T010 [P] Create `AnalyticsService` in `frontend/src/app/core/services/analytics.service.ts` with `getDashboardAnalytics$(): Observable<DashboardAnalytics>` calling `GET /api/analytics/dashboard`. Export from `frontend/src/app/core/services/index.ts`

**Checkpoint**: Backend persists interactions, exposes analytics endpoint. Frontend service ready to consume data.

---

## Phase 3: User Story 1 - View Key Performance Indicators (Priority: P1) MVP

**Goal**: Admin navigates to Analytics page and sees 4 KPI cards: total questions today, average response time, total documents, conversations this week.

**Independent Test**: Navigate to `/analytics`, verify KPI cards show correct aggregated numbers. With no data, cards show zeros gracefully.

### Implementation for User Story 1

- [ ] T011 [US1] Create `analytics-page` container component in `frontend/src/app/features/analytics/components/analytics-page/` (analytics-page.component.ts, .html, .css). Standalone, OnPush, injects `AnalyticsService`, loads dashboard data in `ngOnInit()`, stores result in signal. Shows loading spinner while fetching
- [ ] T012 [US1] Create `kpi-cards` component in `frontend/src/app/features/analytics/components/kpi-cards/` (kpi-cards.component.ts, .html, .css). Standalone, OnPush. Inputs: `totalQuestionsToday`, `averageResponseTimeMs`, `totalDocuments`, `conversationsThisWeek` via `input()`. Displays 4 styled cards with icons and formatted values (ms → seconds for response time)
- [ ] T013 [US1] Add lazy-loaded route `/analytics` in `frontend/src/app/app.routes.ts` loading `AnalyticsPageComponent`
- [ ] T014 [US1] Add "Analytics" nav link with chart SVG icon in `frontend/src/app/layout/header/header.component.html` following existing nav link pattern (routerLink, routerLinkActive, SVG icon)

**Checkpoint**: User Story 1 fully functional — admin can navigate to /analytics and see 4 KPI cards with live data

---

## Phase 4: User Story 2 - View Popular Questions (Priority: P2)

**Goal**: Admin sees a ranked list of the top 10 most asked questions with frequency counts.

**Independent Test**: Create repeated chat interactions with same questions, verify popular questions list shows correct ranking and counts. Empty state when no data.

### Implementation for User Story 2

- [ ] T015 [US2] Create `popular-questions` component in `frontend/src/app/features/analytics/components/popular-questions/` (popular-questions.component.ts, .html, .css). Standalone, OnPush. Input: `questions` via `input<QuestionFrequency[]>()`. Displays ranked list with question text (truncated at 100 chars with tooltip for full text) and count badge. Empty state message when list is empty
- [ ] T016 [US2] Integrate `popular-questions` component into `analytics-page.component.html`, passing `popularQuestions` data from dashboard analytics signal

**Checkpoint**: User Stories 1 AND 2 functional — KPI cards + popular questions list

---

## Phase 5: User Story 3 - View Most Referenced Documents (Priority: P2)

**Goal**: Admin sees a ranked list of the top 10 most referenced documents with reference counts.

**Independent Test**: Perform chat interactions referencing different documents, verify document list shows correct ranking. Deleted documents excluded.

### Implementation for User Story 3

- [ ] T017 [US3] Add `findTopReferencedDocuments()` query to `ChatInteractionRepository` in `backend/src/main/java/com/hrassistant/repository/ChatInteractionRepository.java`. Join with `chat_interaction_documents` table, group by document ID, count references, join with `documents` table for filename, return top 10. Exclude deleted documents (only return IDs that exist in `documents` table)
- [ ] T018 [US3] Update `AnalyticsService` in `backend/src/main/java/com/hrassistant/service/AnalyticsService.java` to populate `topDocuments` field using the new repository query
- [ ] T019 [US3] Create `top-documents` component in `frontend/src/app/features/analytics/components/top-documents/` (top-documents.component.ts, .html, .css). Standalone, OnPush. Input: `documents` via `input<DocumentReference[]>()`. Displays ranked list with document name and reference count badge. Empty state message when list is empty
- [ ] T020 [US3] Integrate `top-documents` component into `analytics-page.component.html`, passing `topDocuments` data from dashboard analytics signal

**Checkpoint**: User Stories 1, 2, AND 3 functional — KPI cards + popular questions + top documents

---

## Phase 6: User Story 4 - View Usage Over Time Chart (Priority: P3)

**Goal**: Admin sees a line/bar chart showing daily question counts for the last 30 days.

**Independent Test**: Generate interactions across multiple days, verify chart shows correct daily counts with zero-fill for inactive days.

### Implementation for User Story 4

- [ ] T021 [US4] Create `usage-chart` component in `frontend/src/app/features/analytics/components/usage-chart/` (usage-chart.component.ts, .html, .css). Standalone, OnPush. Inputs: `dailyUsage` via `input<DailyCount[]>()`, `dailyResponseTimes` via `input<DailyAverage[]>()`. Use PrimeNG `p-chart` (bar chart) for daily usage. Format dates as "DD MMM" labels. Apply Midnight Studio theme colors (cyan/teal accents)
- [ ] T022 [US4] Integrate `usage-chart` component into `analytics-page.component.html`, passing `dailyUsage` data from dashboard analytics signal

**Checkpoint**: User Stories 1-4 functional — KPI cards + lists + usage chart

---

## Phase 7: User Story 5 - View Average Response Time Trend (Priority: P3)

**Goal**: Admin sees a line chart showing daily average response times for the last 30 days.

**Independent Test**: Verify chart shows daily averages matching computed response times from interactions.

### Implementation for User Story 5

- [ ] T023 [US5] Add response time line chart to `usage-chart` component in `frontend/src/app/features/analytics/components/usage-chart/usage-chart.component.ts`. Add a second `p-chart` (line chart) for `dailyResponseTimes` input. Format Y-axis as seconds, X-axis as "DD MMM". Use amber/warm accent color to differentiate from usage chart
- [ ] T024 [US5] Integrate response time chart into `analytics-page.component.html`, passing `dailyResponseTimes` data from dashboard analytics signal

**Checkpoint**: All user stories (1-5) fully functional

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Testing, quality, and edge case handling

- [ ] T025 [P] Create `AnalyticsServiceTest` in `backend/src/test/java/com/hrassistant/service/AnalyticsServiceTest.java` with unit tests: verify KPI aggregation, popular questions ranking, top documents with deleted exclusion, daily counts with zero-fill, empty data handling
- [ ] T026 [P] Create `AnalyticsControllerTest` in `backend/src/test/java/com/hrassistant/controller/AnalyticsControllerTest.java` with MockMvc test for `GET /api/analytics/dashboard` returning 200 with correct JSON structure
- [ ] T027 [P] Create component tests for `analytics-page`, `kpi-cards`, `popular-questions`, `top-documents`, `usage-chart` in their respective `.spec.ts` files. Test loading states, empty states, data rendering, and input bindings
- [ ] T028 Verify backend compiles and all tests pass: `cd backend && mvn clean test`
- [ ] T029 Verify frontend compiles and all tests pass: `cd frontend && ng build && ng test`
- [ ] T030 Run quickstart.md validation: start all services, navigate to `/analytics`, verify full dashboard renders with live data

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Phase 2 completion
  - US1 (P1): Can start immediately after Phase 2
  - US2 (P2): Can start after Phase 2 (frontend-only, independent of US1)
  - US3 (P2): Can start after Phase 2 (needs T017-T018 backend work, then frontend)
  - US4 (P3): Can start after Phase 2 (frontend-only)
  - US5 (P3): Depends on US4 (extends same component)
- **Polish (Phase 8)**: Depends on all user stories complete

### Within Each User Story

- Backend changes (repository queries, service methods) before frontend components
- Container component before child components
- Integration (wiring into page) as final step per story

### Parallel Opportunities

**Backend agents can work in parallel with frontend agents after Phase 2:**
- T009 + T010 (frontend models/service) can run in parallel with T003-T008 (backend)
- T011 + T012 (US1 frontend) can start once T008 (controller) is done
- T015 (US2), T019 (US3), T021 (US4) are independent frontend components — all parallelizable
- T025, T026, T027 (all test tasks) are parallelizable

---

## Parallel Example: Team of 3 Agents

```text
# Phase 2 — Backend agent:
T003 → T004 → T005 → T006 → T007 → T008

# Phase 2 — Frontend agent (in parallel):
T009 + T010 (can start immediately, no backend dependency)

# Phase 3-7 — After Phase 2 complete:
Backend agent: T017 → T018 (US3 backend queries)
Frontend agent: T011 → T012 → T013 → T014 (US1) → T015 → T016 (US2)
Test agent: waits for implementation, then T025 + T026 + T027

# Phase 6-7 — Frontend agent continues:
T019 → T020 (US3 frontend) → T021 → T022 (US4) → T023 → T024 (US5)

# Phase 8 — Test agent:
T025 + T026 + T027 (parallel) → T028 → T029 → T030
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T002)
2. Complete Phase 2: Foundational (T003-T010)
3. Complete Phase 3: User Story 1 (T011-T014)
4. **STOP and VALIDATE**: Navigate to `/analytics`, verify KPI cards
5. Deploy/demo if ready

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add User Story 1 → KPI cards working (MVP!)
3. Add User Stories 2+3 → Popular questions + top documents
4. Add User Stories 4+5 → Charts
5. Polish → Tests + validation

---

## Summary

| Metric | Value |
|--------|-------|
| Total tasks | 30 |
| Phase 1 (Setup) | 2 tasks |
| Phase 2 (Foundational) | 8 tasks |
| US1 (KPI Cards) | 4 tasks |
| US2 (Popular Questions) | 2 tasks |
| US3 (Top Documents) | 4 tasks |
| US4 (Usage Chart) | 2 tasks |
| US5 (Response Time Chart) | 2 tasks |
| Phase 8 (Polish/Tests) | 6 tasks |
| Parallel opportunities | T009+T010, T015+T019+T021, T025+T026+T027 |
| MVP scope | Phase 1-3 (14 tasks) |

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
