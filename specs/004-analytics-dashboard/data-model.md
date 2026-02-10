# Data Model: Analytics Dashboard

**Feature**: 004-analytics-dashboard
**Date**: 2026-02-10

## New Entities

### ChatInteraction

Represents a completed Q&A exchange. Persisted to PostgreSQL for analytics aggregation.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | String (UUID) | PK, auto-generated | Unique interaction identifier |
| question | String | NOT NULL, max 1000 chars | The user's question text |
| responseTimeMs | Long | NOT NULL | Time from request to stream completion (milliseconds) |
| referencedDocumentIds | List\<String\> | nullable | IDs of documents used in RAG response |
| createdAt | LocalDateTime | NOT NULL, indexed | Timestamp of the interaction |

**Table name**: `chat_interactions`

**Indexes**:
- `idx_chat_interactions_created_at` on `createdAt` (for time-range queries)
- `idx_chat_interactions_question` on `LOWER(question)` (for frequency grouping)

**Relationships**:
- `referencedDocumentIds` → references `Document.id` (soft reference, no FK constraint — documents may be deleted)

**Storage**: JPA `@ElementCollection` for `referencedDocumentIds` stored in a join table `chat_interaction_documents`.

## Existing Entities (unchanged)

### Document

No schema changes. Referenced by `ChatInteraction.referencedDocumentIds` via soft reference.

## Analytics DTOs (not persisted)

### DashboardAnalytics

Aggregated response DTO returned by the analytics endpoint.

| Field | Type | Description |
|-------|------|-------------|
| totalQuestionsToday | Long | Count of interactions created today |
| averageResponseTimeMs | Double | Average response time across all interactions |
| totalDocuments | Long | Count of documents in the system |
| conversationsThisWeek | Long | Count of interactions in the current week (Mon-Sun) |
| popularQuestions | List\<QuestionFrequency\> | Top 10 questions by frequency |
| topDocuments | List\<DocumentReference\> | Top 10 documents by reference count |
| dailyUsage | List\<DailyCount\> | Daily question counts for last 30 days |
| dailyResponseTimes | List\<DailyAverage\> | Daily average response times for last 30 days |

### QuestionFrequency

| Field | Type | Description |
|-------|------|-------------|
| question | String | The question text |
| count | Long | Number of times asked |

### DocumentReference

| Field | Type | Description |
|-------|------|-------------|
| documentId | String | Document identifier |
| documentName | String | Document filename |
| referenceCount | Long | Number of times referenced in RAG responses |

### DailyCount

| Field | Type | Description |
|-------|------|-------------|
| date | LocalDate | The day |
| count | Long | Number of interactions that day |

### DailyAverage

| Field | Type | Description |
|-------|------|-------------|
| date | LocalDate | The day |
| averageMs | Double | Average response time in milliseconds |

## Database Migration

No Flyway/Liquibase in use — schema managed by `hibernate.ddl-auto: update`. JPA entity annotations will auto-create:
- `chat_interactions` table
- `chat_interaction_documents` join table
- Required indexes via `@Table(indexes = ...)`
