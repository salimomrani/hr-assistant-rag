# CLAUDE.md

## Project Overview

HR Assistant RAG — intelligent HR assistant using RAG on internal documents.

```
hr-assistant-rag/
├── backend/    # Spring Boot 4.0.1 + Spring AI 2.0.0-M1
├── frontend/   # Angular 21, PrimeNG v21
└── specs/      # Feature specifications
```

## Tech Stack

- **Backend**: Java 21, Spring Boot 4.0.1, Spring AI 2.0.0-M1, WebFlux/SSE
- **LLM**: Ollama (llama3.2 + nomic-embed-text) on port 11434
- **Storage**: PostgreSQL 16 + pgvector (HNSW, cosine), Redis 7.4 (semantic cache)
- **Frontend**: Angular 21, TypeScript 5.9, PrimeNG v21, RxJS 7.8+, ngx-markdown
- **Infra**: Docker Compose (pgvector + Redis)

## Build & Run

```bash
# Backend
cd backend && mvn clean install        # Build
cd backend && mvn spring-boot:run      # Run
lsof -ti:8080 | xargs kill -9         # Kill port 8080

# Frontend
cd frontend && npm start               # Dev server (proxy to backend)
cd frontend && ng test                  # Tests (Vitest)
cd frontend && ng build --configuration=production

# Infrastructure
cd backend && docker compose up -d     # pgvector + Redis
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Health check |
| POST | `/api/chat` | Blocking Q&A |
| POST | `/api/chat/stream` | Streaming SSE Q&A |
| POST | `/api/documents` | Upload document |
| GET | `/api/documents` | List documents |
| DELETE | `/api/documents/{id}` | Delete document |

## Backend Architecture

Layered architecture in `backend/src/main/java/com/hrassistant/`:

- **controller/** — REST endpoints (Chat, Document, Health)
- **service/** — RagService, StreamingRagService, CachingStreamingRagService, EmbeddingService, VectorStoreService, DocumentService, CacheService, GuardrailService
- **model/** — DTOs and JPA entities
- **mapper/** — MapStruct mappers
- **repository/** — Spring Data JPA
- **config/** — Redis, Web, Logging configs

## Coding Standards

- **Comments**: ALWAYS in English, NEVER in French
- **Java**: Lombok, Spring utilities (StringUtils), MapStruct for mappings, records for DTOs, JPA entities with `@Entity` + `@Id`
- **Don't Reinvent the Wheel**: Use existing libraries

## Development Workflow

**IMPORTANT**: Work step-by-step with user validation:
1. Implement ONE file/method at a time
2. Show code to user
3. Wait for validation before proceeding

## Frontend Standards

**IMPORTANT**: Use `/frontend-design` skill for creating UI components.

- Separate .ts, .html, .css files (never inline for feature components)
- `input()`/`output()` functions, `inject()` for DI
- Native control flow (`@if`, `@for`), signals for state
- PrimeNG v21 components exclusively (use Context7 for docs)
- Lazy-loaded feature routes with `loadComponent`

## Spring AI Patterns

- BOM version via `${spring-ai.version}`, Ollama auto-configured via `spring.ai.ollama.*`
- pgvector via `spring-ai-starter-vector-store-pgvector`
- Semantic caching via `CacheService` + `CachingStreamingRagService`
