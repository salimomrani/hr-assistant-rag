# Feature Specification: Analytics Dashboard

**Feature Branch**: `004-analytics-dashboard`
**Created**: 2026-02-10
**Status**: Draft
**Input**: User description: "Analytics Dashboard for the HR Assistant RAG application. Dashboard with stats: popular questions asked by users, average response times, most consulted/referenced documents, daily/weekly usage metrics. Admin-only page accessible from the header navigation. Backend endpoints to aggregate and serve analytics data from existing chat and document interactions. Frontend dashboard with charts and KPIs using PrimeNG components."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Key Performance Indicators (Priority: P1)

An administrator navigates to the Analytics page from the header navigation. They see a dashboard with KPI cards showing at-a-glance metrics: total questions asked today, average response time, total documents in the system, and total conversations this week. The data refreshes each time the page is loaded.

**Why this priority**: KPIs provide the highest-value overview with the least complexity. Even without charts, administrators gain immediate insight into system health and usage.

**Independent Test**: Can be fully tested by navigating to the Analytics page and verifying that KPI cards display correct aggregated numbers matching the underlying data.

**Acceptance Scenarios**:

1. **Given** the admin is on the app and has existing chat interactions, **When** they navigate to the Analytics page, **Then** they see KPI cards with total questions today, average response time, total documents, and conversations this week.
2. **Given** no chat interactions have occurred yet, **When** the admin navigates to the Analytics page, **Then** KPI cards display zero values gracefully (no errors or broken UI).
3. **Given** the admin is on the Analytics page, **When** they reload the page, **Then** the KPI values refresh with the latest data.

---

### User Story 2 - View Popular Questions (Priority: P2)

An administrator wants to understand what employees are asking most frequently. On the Analytics page, they see a ranked list of the most popular questions (top 10), showing the question text and how many times it was asked. This helps HR identify common concerns and knowledge gaps.

**Why this priority**: Understanding what users ask is the core analytical insight for an HR assistant. It directly informs document improvements and HR strategy.

**Independent Test**: Can be tested by creating several chat interactions with repeated questions and verifying the popular questions list reflects correct ranking and counts.

**Acceptance Scenarios**:

1. **Given** multiple users have asked similar questions, **When** the admin views the popular questions section, **Then** questions are ranked by frequency with the most-asked question first.
2. **Given** fewer than 10 unique questions exist, **When** the admin views the section, **Then** only the available questions are shown without empty slots.
3. **Given** no questions have been asked, **When** the admin views the section, **Then** an empty state message is displayed.

---

### User Story 3 - View Most Referenced Documents (Priority: P2)

An administrator wants to know which uploaded documents are most frequently used in RAG responses. The Analytics page shows a ranked list of the top 10 most referenced documents, including the document name and reference count. This helps identify which documents are most valuable and which may need updating.

**Why this priority**: Equal to popular questions in value — understanding document usage helps HR maintain and prioritize their knowledge base.

**Independent Test**: Can be tested by performing chat interactions that reference different documents and verifying the document ranking matches actual reference counts.

**Acceptance Scenarios**:

1. **Given** chat interactions have referenced various documents, **When** the admin views the most referenced documents section, **Then** documents are ranked by reference count, highest first.
2. **Given** a document has been deleted but was previously referenced, **When** the admin views the section, **Then** the deleted document is excluded from the list.
3. **Given** no documents have been referenced, **When** the admin views the section, **Then** an empty state message is displayed.

---

### User Story 4 - View Usage Over Time Chart (Priority: P3)

An administrator wants to see usage trends. The Analytics page includes a chart showing the number of questions asked per day over the last 30 days. This helps identify patterns, peak usage days, and overall adoption trends.

**Why this priority**: Time-series visualization adds deeper insight but depends on having the data aggregation infrastructure from P1/P2 stories in place.

**Independent Test**: Can be tested by generating chat interactions across multiple days and verifying the chart accurately reflects daily counts.

**Acceptance Scenarios**:

1. **Given** chat interactions spanning multiple days, **When** the admin views the usage chart, **Then** a chart displays daily question counts for the last 30 days.
2. **Given** days with no activity within the 30-day range, **When** the admin views the chart, **Then** those days show zero values (no gaps in the timeline).
3. **Given** the system has been running for fewer than 30 days, **When** the admin views the chart, **Then** only the available days are shown.

---

### User Story 5 - View Average Response Time Trend (Priority: P3)

An administrator wants to monitor system performance over time. The Analytics page shows a chart of average response times per day for the last 30 days, helping detect performance degradation or improvement.

**Why this priority**: Performance monitoring is valuable but secondary to understanding what users ask and which documents are used.

**Independent Test**: Can be tested by checking that the chart shows daily averages matching computed response times from chat logs.

**Acceptance Scenarios**:

1. **Given** chat interactions with varying response times, **When** the admin views the response time chart, **Then** a chart displays daily average response times for the last 30 days.
2. **Given** a day with a single very slow response, **When** the admin views the chart, **Then** that day's average accurately reflects the outlier.

---

### Edge Cases

- What happens when the analytics page is loaded while a chat interaction is actively streaming? Analytics should use only completed interactions.
- How does the system handle extremely long questions when displaying popular questions? Questions should be truncated in the display with full text available on hover or click.
- What happens when the dataset is very large (thousands of interactions)? Aggregation should be performed server-side, not client-side, to maintain performance.
- What happens if the analytics endpoint is slow? The dashboard should show loading indicators per section, not block the entire page.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST record each completed chat interaction including the question text, response time, timestamp, and referenced document identifiers.
- **FR-002**: System MUST provide an aggregated count of total questions for the current day.
- **FR-003**: System MUST calculate the average response time across all completed interactions.
- **FR-004**: System MUST rank questions by frequency and return the top 10 most asked questions.
- **FR-005**: System MUST rank documents by reference count and return the top 10 most referenced documents.
- **FR-006**: System MUST aggregate daily question counts for the last 30 days.
- **FR-007**: System MUST aggregate daily average response times for the last 30 days.
- **FR-008**: System MUST expose analytics data through dedicated endpoints.
- **FR-009**: System MUST display analytics on a dedicated page accessible from the main navigation.
- **FR-010**: System MUST show loading states while analytics data is being fetched.
- **FR-011**: System MUST display meaningful empty states when no data is available for a section.
- **FR-012**: System MUST truncate long question text in the popular questions list with the ability to see the full text.

### Key Entities

- **ChatInteraction**: Represents a completed Q&A exchange. Key attributes: question text, response time (milliseconds), timestamp, list of referenced document identifiers.
- **AnalyticsSnapshot**: Aggregated analytics data for the dashboard. Key attributes: total questions today, average response time, total documents, conversations this week, popular questions list, top documents list, daily usage series, daily response time series.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Administrators can view all dashboard KPIs within 2 seconds of navigating to the Analytics page.
- **SC-002**: Popular questions list accurately reflects the top 10 most frequently asked questions with correct counts.
- **SC-003**: Most referenced documents list accurately reflects document usage based on actual RAG responses.
- **SC-004**: Usage trend charts display 30 days of historical data with daily granularity.
- **SC-005**: Dashboard sections load independently, allowing partial display while slower sections are still loading.
- **SC-006**: The analytics page is accessible from the main header navigation.

## Assumptions

- The application currently does not persist chat interactions. A new persistence mechanism will be needed to record interactions for analytics.
- "Administrator" access is determined by the existing navigation structure (the admin/documents page is already accessible). No separate authentication system is required for this feature.
- Response time is measured from when the user sends a question to when the complete response is delivered (end of SSE stream).
- Document reference tracking will leverage the existing RAG pipeline's document retrieval step.
- The 30-day window for time-series data is a rolling window based on the current date.
