# Research: Analytics Dashboard

**Feature**: 004-analytics-dashboard
**Date**: 2026-02-10

## R1: Chat Interaction Persistence Strategy

**Decision**: Use a new JPA entity `ChatInteraction` persisted to PostgreSQL via Spring Data JPA.

**Rationale**: The application already uses PostgreSQL with Spring Data JPA for `Document` entities. Adding a new table is the simplest approach with zero new infrastructure. JPA aggregation queries (`@Query` with `GROUP BY`, `COUNT`, `AVG`) handle all analytics needs server-side.

**Alternatives considered**:
- **Application logs + log parser**: Would require log parsing infrastructure, not real-time queryable, brittle to log format changes. Rejected.
- **Micrometer + Prometheus**: Overkill for this scope — adds two new infrastructure dependencies (Prometheus + Grafana) for 5 metrics. Rejected.
- **Redis time-series**: Would add another Redis data structure. PostgreSQL is already available and better suited for complex aggregation queries. Rejected.

## R2: When to Record Interactions

**Decision**: Record interaction asynchronously after the SSE stream completes in `StreamingRagService` (or `CachingStreamingRagService`).

**Rationale**: Recording must happen after the full response is assembled (to capture response time and referenced documents). Using `doOnComplete` on the Flux ensures zero impact on streaming latency. The interaction is saved asynchronously on `Schedulers.boundedElastic()`.

**Alternatives considered**:
- **Synchronous save before response**: Would add latency to chat response. Rejected.
- **Save in controller layer**: Controller doesn't have access to response time or referenced documents. Rejected.
- **Event-driven (Spring Events)**: Adds unnecessary indirection for a single subscriber. Rejected.

## R3: Question Similarity for "Popular Questions"

**Decision**: Use exact string matching (case-insensitive) with `LOWER(question)` GROUP BY for question frequency counting.

**Rationale**: Simple, performant with a database index, and sufficient for identifying repeated questions. Semantic similarity would require embedding comparison which is expensive and unnecessary for a "top 10" list.

**Alternatives considered**:
- **Embedding-based clustering**: Too expensive for a dashboard query that runs on every page load. Would need pre-computed clusters. Rejected for V1.
- **Fuzzy matching (Levenshtein)**: Complex query, harder to index, marginal benefit over exact match. Rejected for V1.

## R4: Frontend Charting Library

**Decision**: Use Chart.js 4.x via PrimeNG's `p-chart` component.

**Rationale**: PrimeNG already wraps Chart.js with its `ChartModule`. This is the library-first approach per Constitution Principle I. Only requires adding `chart.js` as a dependency — no additional charting library.

**Alternatives considered**:
- **Apache ECharts**: More powerful but adds a large dependency (800KB+) and doesn't integrate with PrimeNG. Rejected.
- **D3.js**: Low-level, requires significant custom code. Violates library-first principle. Rejected.
- **ng2-charts**: Another Chart.js wrapper, but PrimeNG already provides one. Adding a second wrapper is redundant. Rejected.

## R5: Document Reference Tracking

**Decision**: Extract document IDs from the vector search results in `StreamingRagService.extractSources()` and include them in the `ChatInteraction` record.

**Rationale**: The existing `extractSources()` method already identifies which documents were used in the RAG response. We piggyback on this data to populate the `referencedDocumentIds` field.

**Alternatives considered**:
- **Parse source references from response text**: Fragile, depends on response format. Rejected.
- **Separate document tracking service**: Over-engineering for storing a list of IDs. Rejected.

## R6: Analytics Endpoint Design

**Decision**: Single endpoint `GET /api/analytics/dashboard` returning all dashboard data in one response.

**Rationale**: The dashboard loads all sections simultaneously. A single endpoint reduces HTTP overhead and simplifies frontend logic. Server-side aggregation is fast enough (simple SQL queries on indexed columns) to return all data within the 2-second target.

**Alternatives considered**:
- **Multiple endpoints per section**: More HTTP calls, more complex loading states, no real benefit since all data is needed simultaneously. Rejected for V1.
- **GraphQL**: Adds a new paradigm to the project for one endpoint. Overkill. Rejected.

## R7: Response Time Measurement

**Decision**: Measure wall-clock time from request receipt to stream completion using `System.currentTimeMillis()` diff.

**Rationale**: Simple, accurate for user-perceived latency, consistent with the existing `LoggingInterceptor` approach.

**Alternatives considered**:
- **Micrometer Timer**: Adds a dependency for one measurement. Rejected.
- **Reactive elapsed operator**: `Flux.elapsed()` measures inter-element time, not total time. Not suitable. Rejected.
