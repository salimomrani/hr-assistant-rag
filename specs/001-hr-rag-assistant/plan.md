# Implementation Plan: HR RAG Assistant

**Branch**: `001-hr-rag-assistant` | **Date**: 2026-01-21 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-hr-rag-assistant/spec.md`

## Summary

Assistant RH intelligent utilisant RAG (Retrieval-Augmented Generation) pour répondre aux questions des employés basées sur les documents internes. Le système utilise Spring Boot avec LangChain4j pour orchestrer les embeddings, la recherche vectorielle et la génération de réponses via Ollama (llama3.2).

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 4.0.1, LangChain4j 1.10.0, Spring WebFlux
**Storage**: In-Memory (InMemoryEmbeddingStore) - MVP, pgvector prévu ultérieurement
**Testing**: JUnit 5, Spring Boot Test, Mockito
**Target Platform**: Linux/macOS server (local deployment)
**Project Type**: Single backend API (REST + SSE)
**Performance Goals**: Réponse < 10s, indexation 50 pages < 60s, streaming start < 2s
**Constraints**: 50 utilisateurs simultanés, Ollama local requis sur port 11434
**Scale/Scope**: MVP mono-tenant, documents en français uniquement

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| Simplicity | ✅ Pass | Architecture simple: single backend, in-memory storage |
| Test-First | ✅ Pass | Tests unitaires et d'intégration prévus |
| Observability | ✅ Pass | Logging structuré via SLF4J/Logback |

*Constitution template non configurée - principes par défaut appliqués.*

## Project Structure

### Documentation (this feature)

```text
specs/001-hr-rag-assistant/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (OpenAPI)
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
src/main/java/com/hrassistant/
├── HrAssistantApplication.java      # Entry point
├── config/
│   └── OllamaConfig.java            # LangChain4j beans configuration
├── controller/
│   ├── ChatController.java          # POST /api/chat, GET /api/chat/stream
│   ├── DocumentController.java      # CRUD /api/documents
│   └── HealthController.java        # GET /api/health
├── service/
│   ├── RagService.java              # RAG orchestration
│   ├── EmbeddingService.java        # Text to vectors
│   ├── VectorStoreService.java      # Embedding storage & search
│   ├── DocumentService.java         # Upload, chunk, index
│   ├── GuardrailService.java        # Input/output filtering
│   └── CacheService.java            # Semantic caching (optional)
├── model/
│   ├── ChatRequest.java             # DTO: question + conversationId
│   ├── ChatResponse.java            # DTO: answer + sources
│   ├── DocumentInfo.java            # DTO: document metadata
│   └── DocumentChunk.java           # Internal: chunk with metadata
└── exception/
    └── HrAssistantException.java    # Custom exceptions

src/main/resources/
├── application.yml                  # Configuration
└── prompts/
    └── rag-prompt.txt               # System prompt template

src/test/java/com/hrassistant/
├── controller/
│   ├── ChatControllerTest.java
│   └── DocumentControllerTest.java
├── service/
│   ├── RagServiceTest.java
│   ├── EmbeddingServiceTest.java
│   ├── VectorStoreServiceTest.java
│   └── DocumentServiceTest.java
└── integration/
    └── RagIntegrationTest.java
```

**Structure Decision**: Single project structure. Backend-only REST API sans frontend. Les tests sont organisés par couche (controller, service, integration).

## Complexity Tracking

*Aucune violation de constitution détectée - pas de justification requise.*
