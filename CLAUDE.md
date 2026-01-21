# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HR Assistant RAG - An intelligent HR assistant that answers employee questions using RAG (Retrieval-Augmented Generation) on internal documents.

## Tech Stack

- **Backend**: Spring Boot 4.0.1, Java 17
- **AI**: LangChain4j 1.10.0
- **LLM**: Ollama (llama3.2) running locally on port 11434
- **Streaming**: WebFlux / SSE
- **Vector Store**: In-Memory (planned: pgvector)

## Build & Run Commands

```bash
# Build
mvn clean install

# Run application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Kill process on port 8080 if needed
lsof -ti:8080 | xargs kill -9
```

## Prerequisites

Ollama must be running locally:
```bash
ollama run llama3.2
```

## Architecture

The application follows a layered architecture:

1. **Controllers** (`controller/`) - REST endpoints for chat and document management
2. **Services** (`service/`) - Business logic
   - `RagService` - Orchestrates the RAG pipeline
   - `EmbeddingService` - Converts text to vectors
   - `VectorStoreService` - Stores and searches embeddings
   - `DocumentService` - Handles document upload and chunking
   - `GuardrailService` - Input/output filtering
   - `CacheService` - Semantic caching
3. **Config** (`config/`) - Spring configuration beans for LangChain4j
4. **Model** (`model/`) - DTOs (ChatRequest, ChatResponse, DocumentInfo)

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chat` | Send question, get full response |
| GET | `/api/chat/stream` | Send question, get streaming SSE response |
| POST | `/api/documents` | Upload document |
| GET | `/api/documents` | List indexed documents |
| DELETE | `/api/documents/{id}` | Delete document |

## Development Phases

See `SPEC.md` for detailed development roadmap. Current phase: Phase 1 (Core RAG).

## Coding Standards

- **Comments**: ALWAYS write comments in English, NEVER in French
- **Javadoc**: Use English for all documentation
- **Code**: Use Lombok annotations to reduce boilerplate

## LangChain4j Patterns

- Use `${langchain4j.version}` property for version consistency
- Embedding models and chat models are configured as Spring beans in `OllamaConfig`
- Vector store uses `InMemoryEmbeddingStore` for development

## Active Technologies
- Java 17 + Spring Boot 4.0.1, LangChain4j 1.10.0, Spring WebFlux (001-hr-rag-assistant)
- In-Memory (InMemoryEmbeddingStore) - MVP, pgvector prévu ultérieuremen (001-hr-rag-assistant)

## Recent Changes
- 001-hr-rag-assistant: Added Java 17 + Spring Boot 4.0.1, LangChain4j 1.10.0, Spring WebFlux
